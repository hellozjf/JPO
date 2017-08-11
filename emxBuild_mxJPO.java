/*
**  emxBuild.java
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

import  matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


/**
 * This JPO class has some methods pertaining to Build type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxBuild_mxJPO extends emxBuildBase_mxJPO
{
    /** Alias for key emxProduct.Error.UnsupportedClient. */
    protected static final String ERROR_UNSUPPORTEDCLIENT = "emxProduct.Error.UnsupportedClient";

    /**
     * Create a new emxBuildobject from a given id.
     *
	 * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a emxImage
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public emxBuild_mxJPO (Context context, String[] args) throws Exception
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
     * @since ProductCentral 10.0.0.0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
        	String sContentLabel = EnoviaResourceBundle.getProperty(context, "ProductLine",ERROR_UNSUPPORTEDCLIENT, context.getSession().getLanguage());
            throw  new Exception(sContentLabel);
        }
        return  0;
    }
}
