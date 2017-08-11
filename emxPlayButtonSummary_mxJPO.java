import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.History;
import matrix.db.HistoryItr;
import matrix.db.HistoryList;
import matrix.db.JPO;
import matrix.db.NameValue;
import matrix.db.NameValueItr;
import matrix.db.NameValueList;

import com.matrixone.apps.domain.util.MapList;


public class emxPlayButtonSummary_mxJPO  extends emxPlayButtonSummaryBase_mxJPO {

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 2014
     * @grade 0
     */
    public emxPlayButtonSummary_mxJPO (Context context, String[] args)
        throws Exception
    {
    	super(context, args);
    }
}
