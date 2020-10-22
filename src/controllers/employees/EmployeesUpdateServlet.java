package controllers.employees;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;
import models.validators.EmployeeValidator;
import utils.DBUtil;
import utils.EncryptUtil;

/**
 * Servlet implementation class EmployeesUpdateServlet
 */
@WebServlet("/employees/update")
public class EmployeesUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmployeesUpdateServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */

    //edit.jspから送られたデータを受け取る
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String _token = (String)request.getParameter("_token");
        //_tokenパラメータを取得してそれがnull以外でセッションIDが一致した場合実行
        if(_token != null && _token.equals(request.getSession().getId())) {
            EntityManager em = DBUtil.createEntityManager();

            //EditSErvletからセッションスコープに保存したemployee_idを取得してInteger型に変換
            Employee e = em.find(Employee.class, (Integer)(request.getSession().getAttribute("employee_id")));



            Boolean code_duplicate_check = true;
            //入力したcodeと元々あるcodeが一致した場合falseを返す
            if(e.getCode().equals(request.getParameter("code"))) {
                code_duplicate_check = false;
            } else {
                e.setCode(request.getParameter("code"));
            }


            Boolean password_check_flag = true;
            String password = request.getParameter("password");

            //passwordがnullか空白の場合はfalseを返して
            if(password == null || password.equals("")) {
                password_check_flag = false;
            } else {

                //それ以外の場合はパスワードが重複していないかアプリケーションスコープを呼び出して確認する
                e.setPassword(EncryptUtil.getPasswordEncrypt(password,(String)this.getServletContext().getAttribute("pepper")));
            }

            e.setName(request.getParameter("name"));
            e.setAdmin_flag(Integer.parseInt(request.getParameter("admin_flag")));
            e.setUpdated_at(new Timestamp(System.currentTimeMillis()));
            e.setDelete_flag(0);

            //入力された値がnullか空白の場合バリデーションクラスを実行してそれ以外の場合は
            //トランザクションをいつかってデータベースにデータを保存する
            List<String> errors = EmployeeValidator.validate(e, code_duplicate_check, password_check_flag);
            if(errors.size() > 0) {
                em.close();

                request.setAttribute("_token", request.getSession().getId());
                request.setAttribute("employee", e);
                request.setAttribute("errors", errors);

                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/edit.jsp");
                rd.forward(request, response);
            } else {
                em.getTransaction().begin();
                em.getTransaction().commit();
                em.close();
                request.getSession().setAttribute("flush", "更新が完了しました。");

                request.getSession().removeAttribute("employee_id");
                response.sendRedirect(request.getContextPath() + "/employees/index");
            }
        }
    }

}