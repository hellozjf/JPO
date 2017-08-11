import java.util.Map;

// ${CLASSNAME}.java
//
// Created on Dec 29, 2009
//
// Copyright (c) 2005 MatrixOne Inc.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

/**
 * @author SG2
 *
 * The <code>${CLASSNAME}</code> class/interface contains ...
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxSortFileSummaryTableFileNameColumnBase_mxJPO extends
        emxSortHTMLAnchorElement_mxJPO {

    public int compare(Object object1, Object object2) {
        Map map1 = (Map)object1;
        Map map2 = (Map)object2;

          /*Get sort info keys*/
        Map sortKeys = getSortKeys();

        String keyName = (String) sortKeys.get("name");
        String keyDir  = (String) sortKeys.get("dir");

        String stringValue1 = (String) map1.get(keyName);
        String stringValue2 = (String) map2.get(keyName);

        boolean str1Empty = isEmpty(stringValue1);
        boolean str2Empty = isEmpty(stringValue2);

        /*
         * If both values are null or empty diff =  0
         * If first string is null or empty diff = -1
         * If second string is null or empty diff = 1
         * If both the strings are not empty then compare the strings
        */
        int diff = str1Empty && str2Empty ?  0 :
                                str1Empty ? -1 :
                                str2Empty ?  1 :
                                compareAnchorDataPart(getFileNameAnchorPart(stringValue1), getFileNameAnchorPart(stringValue2));

        /* If the direction is not ascending, then invert the sign of 'diff' value
         * */
        return "ascending".equals(keyDir)? diff : -diff ;
    }

    private String getFileNameAnchorPart(String string) {

        /*convert the string to lower case, we are not taking case into consideration.*/
        string = string.toLowerCase();

        /**
         * this data will be in the form of
         * <nobr>
         *      <a href="JavaScript:showModalDialog('emxSuiteDirectory=components', '700', '600', 'false', 'popup', '')"><img src='iconSmallDocument.gif' border=0></a>
         *      &#160;
         *      <a href="JavaScript:showModalDialog('emxSuiteDirectory=components', '700', '600', 'false', 'popup', '')">ntuser.dat.LOG</a>
         *  </nobr>
         *
         *  This method will return
         *  <a href="JavaScript:showModalDialog('emxSuiteDirectory=components', '700', '600', 'false', 'popup', '')">ntuser.dat.LOG</a>
         */

        int anchorBegin = string.lastIndexOf("<a ");
        int anchorEnd = string.lastIndexOf("</a>");
        /**
         * If the string is not of above specified format i.e. does not have '<a' or '</a>' then return the original string.
         */

        if(anchorBegin == -1 || anchorEnd == -1)
            return string;

        /**
         * Now return the last anchor part from the string
         */
        string = string.substring(anchorBegin, anchorEnd + 4);
        return string;
    }


}
