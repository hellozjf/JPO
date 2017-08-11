/*
**  emxPLCCommon.java
**
** Copyright (c) 1999-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
** static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/custom/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
*/

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.i18nNow;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.DomainObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.dassault_systemes.enovia.e6w.foundation.jaxb.FieldValue;
import com.dassault_systemes.enovia.e6w.foundation.ServiceBase;

/**
 * This JPO class is Wrapper JPO for emxPLCCommonBase JPO which is common utility JPO in Product Central.
 * @author Wipro
 * @version ProductCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class PLCWidgetsBase_mxJPO extends emxPLCCommonBase_mxJPO {
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
    public PLCWidgetsBase_mxJPO (Context context, String[] args) throws Exception{
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
     * Gets the number of non-completed tasks for a given phase. The current phase is stored in the project map.
     * The phase is expanded (expand to end) till end to determine the # of tasks.
     *
     * @param context - the eMatrix <code>Context</code> object
     * @return the same map list provided by the widget - MapList
     * @throws Exception if the operation fails
     */
    static public MapList getEvolutionCount(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);         
        String fieldkey = (String) programMap.get(ServiceBase.JPO_WIDGET_FIELD_KEY);
        MapList models = (MapList) programMap.get(ServiceBase.JPO_WIDGET_DATA);        
        StringList busSelects = null;         
        StringList relSelects = null;

        for (int i=0; i < models.size(); i++) {
            Map<String, Object> modelsInfo = (Map<String, Object>) models.get(i);
            String modelId = (String) modelsInfo.get(DomainObject.SELECT_ID);
            
            //String strEvolutionCount= MqlUtil.mqlCommand(context, "eval expr 'count TRUE' on expand bus  "+modelId+"  from rel '" +PropertyUtil.getSchemaProperty("relationship_Products")+"' ");
            
            String strString = "count TRUE";
			String strFromRel = PropertyUtil.getSchemaProperty(context,"relationship_Products");
			String strMQL = "eval expr $1 on expand bus $2 from rel $3";
			String strEvolutionCount= MqlUtil.mqlCommand(context, strMQL, strString, modelId, strFromRel);
			
            modelsInfo.put(fieldkey, strEvolutionCount);
        }        

        return models;
    }
}

