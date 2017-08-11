package framework.services.soap;

import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.framework.services.*;

import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class ui3_mxJPO implements MatrixService {

  TableService tableService = new TableService();
  MenuService menuService = new MenuService();

  public ui3_mxJPO (Context context, String[] args) {
  }

/////////////////////////////////////////////////////////////////////////////////////

  public Map tableCommand(HttpServletRequest request, String name, HashMap requestMap) throws Exception
  {
    return tableService.tableCommand(request, name, requestMap);
  }

  public Map pageTable(HttpServletRequest request, String id, String pageNumber) throws Exception
  {
    return tableService.pageTable(request, id, pageNumber);
  }

  public Map sortTable(HttpServletRequest request, String id, String sortColumnName, String direction) throws Exception
  {
    return tableService.sortTable(request, id, sortColumnName, direction);
  }

  public Map filterTable(HttpServletRequest request, String id, String filter) throws Exception
  {
    return tableService.filterTable(request, id, filter);
  }

//////////////////////////////////////////////////////////////////////////

  public ArrayList getTreeMenu(HttpServletRequest request, HttpServlet servlet, String typeName) throws Exception
  {
    return menuService.getTreeMenu(request, servlet, typeName);
  }

  public ArrayList filterMenu(HttpServletRequest request, String menuName) throws Exception
  {
    return menuService.filterMenu(request, menuName);
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public Map getMenu(Context context, String name) throws Exception
  {
    return UICache.getMenu(context, name);
  }

  public Map getCommand(Context context, String name) throws Exception
  {
    return UICache.getCommand(context, name);
  }

  public Map getTable(Context context, String name) throws Exception
  {
    return UICache.getTable(context, name);
  }

  public Map getForm(Context context, String name) throws Exception
  {
    return UICache.getForm(context, name);
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
