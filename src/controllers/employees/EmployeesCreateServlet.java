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
 * Servlet implementation class EmployeesCreateServlet
 */
@WebServlet("/employees/create")
public class EmployeesCreateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmployeesCreateServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //_form.jspから送られたセッションIDを取得する
        String _token = (String)request.getParameter("_token");
        //セッションIDがある場合、_tokenがセッションIDと一致した場合実行
        if(_token != null && _token.equals(request.getSession().getId())) {
            //EntityManagerクラスのインスタンスを作る
            EntityManager em = DBUtil.createEntityManager();

            Employee e = new Employee();

            //送られてきたセッションIDからcodeとnameをセッターを使ってセットする
            e.setCode(request.getParameter("code"));
            e.setName(request.getParameter("name"));
            //passwordをセットしてSHA256でハッシュ化
            e.setPassword(
                EncryptUtil.getPasswordEncrypt(
                    request.getParameter("password"),
                        (String)this.getServletContext().getAttribute("pepper")
                    )
                );
            e.setAdmin_flag(Integer.parseInt(request.getParameter("admin_flag")));

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            e.setCreated_at(currentTime);
            e.setUpdated_at(currentTime);
            e.setDelete_flag(0);

            //String型のListを作るEmployeeValidaterクラスのvalidateメソッドを使う
            //入力されたデータが無い時に表示するため
            List<String> errors = EmployeeValidator.validate(e, true, true);

            //リストの大きさが０より大きい時実行
            //つまり入力されたデータが無い時
            if(errors.size() > 0) {
                em.close();

                request.setAttribute("_token", request.getSession().getId());
                request.setAttribute("employee", e);
                request.setAttribute("errors", errors);

                //それをnew.jspに送り表示する
                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/new.jsp");
                rd.forward(request, response);
            } else {
                //入力されたデータを受け取って保存するそのままindexServletに移動
                em.getTransaction().begin();
                em.persist(e);
                em.getTransaction().commit();
                request.getSession().setAttribute("flush", "登録が完了しました。");
                em.close();

                response.sendRedirect(request.getContextPath() + "/employees/index");
            }
        }
    }

}