/*
 * emxRequirementSpecification
 *
 * Copyright (c) 2008-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: emxRequirementSpecification.java
 */

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


/**
 * This JPO class has some method pertaining to Requirement Specification type
 * @version 
 */
public class emxRequirementSpecification_mxJPO extends emxRequirementSpecificationBase_mxJPO {

    /**
     * Create a new emxRequirementSpecification object from a given id
     *
     * @param context context for this request
     * @param arg[0] the objectid
     * @return a emxRequirementSpecification
     * @exception Exception when unable to find object in the ProductCentral
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public emxRequirementSpecification_mxJPO(Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point
     *
     * @param context context for this request
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @exception Exception when problems occurred in the ProductCentral
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String language = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed");
            throw  new Exception(strContentLabel);
        }
        return  0;
    }
}



