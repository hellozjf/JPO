/*   emxTagLineage.
 **
 **   Copyright (c) 2002-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program.
 **
 **   This JPO contains the implementation of emxTagLineage.
 **
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.matrixone.search.index.Indexer;
import com.matrixone.search.index.impl.TagLineage;
import com.matrixone.apps.domain.util.PropertyUtil;


import matrix.db.*;
import matrix.util.*;

/**
 * The <code>emxTagLineage</code> class contains implementation code for
 * emxTagLineage.
 *
 * @version AEF 2013x
 */

public class emxTagLineage_mxJPO
{
 
  public emxTagLineage_mxJPO(Context ctx, String[] args)
  {
  }


  public static String getLineage(Context ctx, String [] args) throws Exception
  {
      return TagLineage.getLineage(ctx, args);
  }
  public static String getPredicateForTypeWithHierarchy(Context ctx, String [] args) throws Exception
  {
      return TagLineage.getPredicateForTypeWithHierarchy(ctx, args);
  }

  public static String getPredicateForType(Context ctx, String [] args) throws Exception
  {
      return TagLineage.getPredicateForType(ctx, args);
  }
}
