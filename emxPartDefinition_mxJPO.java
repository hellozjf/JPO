/*
 ** ${CLASS:MarketingFeature}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import matrix.db.Context;

/**
 * The <code>emxPartDefinition</code> class contains code for the following types.:
 *
 *     CAD Drawing, CAD Model and Drawing Print.
 *
 * @version EC Rossini - Copyright (c) 2003, MatrixOne, Inc.
 */

  public class emxPartDefinition_mxJPO extends emxPartDefinitionBase_mxJPO
  {

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC Rossini.
     */

     public emxPartDefinition_mxJPO (Context context, String[] args)
         throws Exception
     {
         super(context, args);
     }
  }
