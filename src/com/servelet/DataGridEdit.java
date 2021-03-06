package com.servelet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dao.TableDao;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet implementation class DataGridEdit
 */
@WebServlet("/dataEdit")
public class DataGridEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(DataGridEdit.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataGridEdit() {
        super();
        // TODO Auto-generated constructor stub
    }
    HashMap<String,String> editRow = new HashMap<String, String>();
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");
		String flag = request.getParameter("flag");
		Map<String, String[]> reqMap = new HashMap<String, String[]>();
		if(flag == null){
			return;
		}
		reqMap.putAll(request.getParameterMap());
		reqMap.remove("flag");
		String target = request.getParameter("target");
		PrintWriter out = response.getWriter();
		if(target != null&&target.equalsIgnoreCase("editing")){
			String oldRow = request.getParameter("oldRow").replaceAll("\\[|\\]", "");	
			//System.out.println(oldRow);
			this.editRow.put(flag,oldRow);
			//System.out.println(this.editRow.get(flag));
			out.write("accepted!");
			out.flush();
			out.close();
			return;
		}
		//PrintWriter out = response.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		String json = mapper.writeValueAsString(reqMap);
		//json = json.replaceAll("\\[|\\]", "");
		String old = this.editRow.get(flag);
		HashMap<String,String> temp = new HashMap<String, String>();
		temp.put("old", old);
		temp.put("new", json);
		json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(temp);
		String path = request.getServletContext().getRealPath("/WEB-INF/classes/com/mod/");
		//System.out.println(json);
		Object result = null;
		String errowMsg = null;
		String traceMsg = null;
		try {
				result = TableDao.reflect(flag, path, json, "updateByObj");
			
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("更新失败：--------------》");
			log.warn(e.getMessage());
			errowMsg = "更新失败！！！";
			//e.printStackTrace();
			StringWriter sw = null;
			PrintWriter pw = null;
			try{
				sw = new StringWriter();
				pw = new PrintWriter(sw, true);
				e.printStackTrace(pw); 
				traceMsg = sw.toString();
				int index = traceMsg.indexOf("Exception");
				errowMsg = traceMsg.substring(0, index);
				errowMsg = errowMsg.replaceAll("\"|\\[|\\}|\\]|\\{|\\s*|\r\n", "");
			}catch(Exception e2){
				e.printStackTrace();
			}finally {
				if(sw != null){
					sw.close();
				}
				if(pw != null){
					pw.close();
				}
			}
			//e.printStackTrace(s);
			
		}finally {
		//	System.out.println(errowMsg);
		//	System.out.println(traceMsg);
			if(traceMsg != null){
				//替换影响json解析的字符串
				traceMsg = traceMsg.replaceAll("\"|\\[|\\}|\\]|\\{|\\s*|\r\n", "");
				out.write("{\"isError\":\"true\",\"msg\":\""+errowMsg+"\",\"trace\":\""+traceMsg+"\"}");
				out.flush();
				out.close();
				return;

			}else if(errowMsg != null){
				out.write("{\"isError\":\"true\",\"msg\":\""+errowMsg+"\"}");
				out.flush();
				out.close();
				return;
			}
		}
		log.info("update result:--------------->"+result);
		if(result != null&&(int)result!=0){
			json = "{\"success\":\"true\"}";
			out.write(json);
			out.flush();
			
		}else{
			out.write("{\"isError\":\"true\",\"msg\":\"更新失败！！！\"}");
			out.flush();
		}
		//System.out.println(json);
		out.close();
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
