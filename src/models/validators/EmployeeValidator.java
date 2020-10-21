package models.validators;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import models.Employee;
import utils.DBUtil;

public class EmployeeValidator {
    //String型のListを作成して引数はEmployee型とboolean型
    public static List<String> validate(Employee e, Boolean code_duplicate_check_flag, Boolean password_check_flag) {
        //String型のListを作る
        List<String> errors = new ArrayList<String>();

        //
        String code_error = _validateCode(e.getCode(), code_duplicate_check_flag);
        if(!code_error.equals("")) {
            errors.add(code_error);
        }

        String name_error = _validateName(e.getName());
        if(!name_error.equals("")) {
            errors.add(name_error);
        }

        String password_error = _validatePassword(e.getPassword(), password_check_flag);
        if(!password_error.equals("")) {
            errors.add(password_error);
        }

        return errors;
    }

    //String型のcodeとBoolean値がtrueなら実行
    private static String _validateCode(String code, Boolean code_duplicate_check_flag) {

        //もしcodeがnullで空白ならば社員番号を入力してくださいを返す
        if(code == null || code.equals("")) {
            return "社員番号を入力してください。";
        }

        //引数にtrueを入れることで下記のプログラムを実行
        if(code_duplicate_check_flag) {
            //EntityManagerクラスのインスタンスを作る
            EntityManager em = DBUtil.createEntityManager();

            //NameQueryで同じコードが何件あるのかカウントする
            //setParameterにセットしたcodeという値が何件あるのかカウントする
            //getSingleResultは件数だけを取得するのでシングルで大丈夫
            long employees_count = (long)em.createNamedQuery("checkRegisteredCode", Long.class)
                                           .setParameter("code", code)
                                             .getSingleResult();
            em.close();

            //取得した件数が０件より多かった場合入力された社員番号の情報はすでに存在していますを返す
            if(employees_count > 0) {
                return "入力された社員番号の情報はすでに存在しています。";
            }
        }

        return "";
    }

    //社員名がnullか空白だった場合、氏名を入力してくださいを返す
    private static String _validateName(String name) {
        if(name == null || name.equals("")) {
            return "氏名を入力してください。";
        }

        return "";
    }

    private static String _validatePassword(String password, Boolean password_check_flag) {
        //パスワードがnullか空白でありpassword_check_flagがtrue場合実行
        if(password_check_flag && (password == null || password.equals(""))) {
            return "パスワードを入力してください。";
        }
        return "";
    }
}