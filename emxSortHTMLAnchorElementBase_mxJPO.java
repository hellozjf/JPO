/*
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.6.2.1 Fri Dec  5 04:05:57 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.6 Wed Oct 22 15:52:50 2008 przemek Experimental przemek $
*/


import java.util.*;

import com.matrixone.apps.domain.util.FrameworkUtil;

import matrix.db.Context;

/**
 * @author KP2 * 
 * The <code>emxSortHTMLAnchorElementBase</code> class contains methods to compare HTML anchor tag contents. Ex: <a herf="abc">Value</a>
 *   
 * This JPO provides comparator for the elements with the anchor tag as an input.
 * It will extract the value from the anchor tag and comparison will take place based on the value.
 * Example:  If <a href="xyz">VALUE1</a> and <a href="xyz">VALUE2</a> are two inputs,
 * then it will extract the values (VALUE1 and VALUE2) from anchor tag and return the comparision result of VALUE1 and VALUE2 only.
 * 
 * if anchor tag is not present in the input, complete string will be considered as value.
 * 
 * The comparision of values will take place as described below:
 * 
 * 1) If both the values are numbers, then it will compare the values as real numbers
 * 2) Else the values for comaparison will be considered as strings
 * 3) if the Value contains any HTML tag inside
 *    Example: <a href="xyz"><img src="abc"/></a>
 *             that is Value = <img src="abc"/> ;
 *             then also this will be considered as string value for the comparison
 * 4) If any of the Html Codes below is the part of the Value, 
 *    that code will be converted to its HTML Display equivalent and then will be compared as a string
 *    HTML_CODE =       {"&nbsp;", "&lt;", "&gt;", "&quot;", "&amp;"};
 *    HTML_DISPLAY=     {" "     , "<"   , ">"   , "\""    , "&"    };
 *     
 * Example:
 * 
 * When this comparator is used for the sorting anchor tag elements in ascending order with below input elements
 * --------------------------------------------------------
 * <a href='abc'>WS&nbsp;1</a>,
 * <a href='abc'>All</a>,
 * <a href='abc'>5&gt;4</a>,
 * <a> </a>,
 * <a><</a>,
 * <a>&gt;</a>,
 * <a><img src='abc' /></a>,
 * <a>1.2</a>,
 * ---------------------------------------------------------
 * the result after sorting will be like below
 * <a> </a>,
 * <a>1.2</a>,
 * <a href='abc'>5&gt;4</a>,
 * <a><</a>,
 * <a><img src='abc' /></a>,
 * <a>&gt;</a>,
 * <a href='abc'>All</a>,
 * <a href='abc'>WS&nbsp;1</a>,
 * ------------------------------------------------------------
 */

public class emxSortHTMLAnchorElementBase_mxJPO extends emxCommonBaseComparator_mxJPO
{

    /**
     * These two arrays contains the HTML Character Entities, CODE and the Display value.
     * e.g. HTML char code <code>&lt;</code> will dispaly '<' when writing to the browser.
     * This sort program check for the existance of any of these char codes in the anchor element and replace them with their display value. 
     * Need to update both the arrays <code>_HTML_CODE</code> and <code>_HTML_DISPLAY</code>, if any new characters are added to this list.
     */
   
    private static final String[] _HTML_CODE =      {"&nbsp;", "&lt;", "&gt;", "&quot;", "&amp;"};
    private static final String[] _HTML_DISPLAY=     {" "     , "<"   , ">"   , "\""    , "&"};
    
    
    public emxSortHTMLAnchorElementBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * Default Constructor.
     */

    public emxSortHTMLAnchorElementBase_mxJPO ()
    {
    }

    /**
     * This method used for comparing objects.
     *
     * @param object1 Map contains column Values
     * @param object2 Map contains column Values
     * @return integer representing the comparision value
     */

    public int compare (Object object1,Object object2)
    {
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
                                compareAnchorDataPart(stringValue1, stringValue2); 
                    
        /* If the direction is not ascending, then invert the sign of 'diff' value
         * */
        return "ascending".equals(keyDir)? diff : -diff ;
    }
    
    public int compareAnchorDataPart(String anchorElement1, String anchorElement2) {
        String data1 = getAnchorTagDataPart(anchorElement1);
        String data2 = getAnchorTagDataPart(anchorElement2);       
        /*check for any html codes in the string and replace them with actual display value.*/
        for (int i = 0; i < _HTML_CODE.length; i++) {            
            data1 = FrameworkUtil.findAndReplace(data1, _HTML_CODE[i], _HTML_DISPLAY[i]);
            data2 = FrameworkUtil.findAndReplace(data2, _HTML_CODE[i], _HTML_DISPLAY[i]);
        }
        data1 = data1.trim();
        data2 = data2.trim();
        
        int diff = 0;
        try
        {
            /*Compare if data is Number*/
            diff = Double.valueOf(data1).compareTo(Double.valueOf(data2));          
        }
        catch (Exception e)
        {
            /*In case of non- numeric value do String comparison*/
            diff = data1.compareTo(data2);
        }
    return diff;
    }

    private String getAnchorTagDataPart(String string) {
        /*convert the string to lower case, we are not taking case into consideration.*/
        string = string.toLowerCase();
        
        /*this data should be in the form of <a herf="action.jsp&objected=1.1.1.1">Value</a>*/
        int anchorBegin = string.indexOf("<a ");
        int anchorEnd = string.indexOf("</a>");
        /*If the string is not of above specified format i.e. not begining with '<a' or ending with '</a>' 
        then return the original string.*/ 
        if(anchorBegin == -1 || anchorEnd == -1)
            return string;
        /*
         * Remove the </a> part from the stirng
         * Now the string becomes <a herf="action.jsp&objected=1.1.1.1">Value
         * */
        string = string.substring(anchorBegin, anchorEnd);
        /*Now get the index of '>' and return the sub string of string.indexOf('>') + 1*/
        string = string.substring(string.indexOf('>') + 1);
        
        return string;
    }    

    public boolean isEmpty(String checkValue) {
        return checkValue == null || checkValue.equals(""); 
    }
}
