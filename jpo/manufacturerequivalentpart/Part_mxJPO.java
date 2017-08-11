package jpo.manufacturerequivalentpart;
// Part.java
// Copyright Dassault Systemes, 2007. All rights reserved
// This program is proprietary property of Dassault Systemes and its subsidiaries.
// This documentation shall be treated as confidential information and may only be used by employees or contractors
//  with the Customer in accordance with the applicable Software License Agreement
//  static const char RCSID[] = $Id: /ENOManufacturerEquivalentPart/CNext/Modules/ENOManufacturerEquivalentPart/JPOsrc/custom/jpo/manufacturerequivalentpart/${CLASSNAME}.java 1.2.2.1.1.1 Wed Oct 29 22:14:50 2008 GMT przemek Experimental$

import matrix.db.Context;

/**
 *
 * The <code>Part</code> class/interface contains ...
 *
 *  Copyright (c) 2007-2016 Dassault Systemes..
 */
public class Part_mxJPO extends jpo.manufacturerequivalentpart.PartBase_mxJPO{

    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Part JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public Part_mxJPO (Context context, String[] args)
    throws Exception
{
        
        // Call the super constructor
        super(context, args);
    }

}
