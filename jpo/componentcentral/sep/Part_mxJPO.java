package jpo.componentcentral.sep;
// (c) Dassault Systemes, 1993-2010.  All rights reserved.

import matrix.db.*;
import matrix.util.*;

import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.componentcentral.CPCConstants;

/**
 *
 * The <code>Part</code> class in ComponentCentral...
 *
 * Copyright (c) 2007-2016 Dassault Systemes..
 */
public class Part_mxJPO extends jpo.componentcentral.sep.PartBase_mxJPO {

    /**
     * Constructs a new PartBase JPO object.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     */

    public Part_mxJPO(Context context, String[] args) throws Exception {
        super(context,args);
    }
}
