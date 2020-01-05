package kr.ac.ajou.esd.calculator;

import java.util.ArrayList;
import java.util.Stack;
//TODO java 는 변수는 기본적으로 camelCase, class는 PascalCase 사용
public class CalUtil {
	//TODO Use enum class
    private static final int NUM = 0; // 숫자
    private static final int LEFT = 1; // '('
    private static final int RIGHT = 2; // ')'
    private static final int OP = 3; // 연산자
    private static final int ROOT = 4; // 루트
	private static int getType(String expr) {
        if (expr.equals("("))
            return LEFT;
        if (expr.equals(")"))
            return RIGHT;
		if (expr.equals("+"))
            return OP;
        if (expr.equals("-"))
            return OP;
        if (expr.equals("*"))
            return OP;
        if (expr.equals("/"))
            return OP;
        if (expr.equals("<"))
            return OP;
        if (expr.equals(">"))
            return OP;
        if (expr.equals("|"))
            return OP;
        if (expr.equals("&"))
            return OP;
        if (expr.equals("√"))
            return ROOT;
        if (expr.equals("^"))
            return OP;
        return NUM;
    }
    private static int getPrecedence(String op) {//연산 우선순위 
        if (op.equals("|"))
            return 1;
        else if (op.equals("&"))
            return 1;
        else if (op.equals(">"))
            return 2;
        else if (op.equals("<"))
            return 2;
        else if (op.equals("+"))
            return 3;
        else if (op.equals("-"))
            return 3;
        else if (op.equals("*"))
            return 4;
        else if (op.equals("/"))
            return 4;
        else if (op.equals("^"))
            return 5;
        return 0;
    }
    private static ArrayList<String> parse(String expr){
        if (getType(expr.substring(expr.length() - 1)) == OP)// 마지막 글자가 연산자이면 null을 리턴 
            return null;
        //모든 기호는 buf를 거쳐서 result로 들어감. 두자리 이상의 숫자와 음수를 하나의 토큰으로 저장해줌.
        //숫자가 들어올 때마다 buf에 넣어주고 연산자가 들어오면 숫자를 result에 넣고 연산자도 result에 넣음.    
        ArrayList<String> result = new ArrayList<String>();
        StringBuilder buf = new StringBuilder(32);

        for (int i = 0; i < expr.length(); i++) {
            char ch = expr.charAt(i);
            if ((ch >= '0' && ch <= '9') || ch == '.') {
                buf.append(ch);//숫자가 ch로 들어오면 계속 buf에 append한다.
            } else if (ch == '(' || ch == ')') {
                //buf에 숫자가 들어있으면 buf의 숫자를 result에 넣어주고 buf의 숫자를 삭제한다.
                if (buf.length() > 0) {  
                    result.add(buf.toString());
                    buf.delete(0, buf.length()+1);
                }//'(',')'를 result에 넣어준다. 
                buf.append(ch);
                result.add(buf.toString());
                buf.delete(0, buf.length()+1);
            } else if (ch == '-') {
                if (i == 0) {
                    buf.append(ch);//마이너스 부호 
                } else {
                    char c = expr.charAt(i - 1);
                    if (c == '(' || c == '*' || c == '/' || c == '^')//마이너스 부호 
                        buf.append(ch);// '-' 부호가 맨처음에 나오거나 다른 연산자 뒤에 나올 경우 마이너스 부호로 인식
                    else {// 빼기 연산 
                        if (buf.length() > 0) {
                            result.add(buf.toString());
                            buf.delete(0, buf.length()+1);
                        }
                        buf.append(ch);
                        result.add(buf.toString());
                        buf.delete(0, buf.length()+1);
                    }
                }
            } else if (ch == '+' || ch == '*' || ch == '/' || ch == '^' || ch == '<' || 
            		ch == '>' || ch == '&' || ch == '√' || ch == '|') {
                if(i==0 && ch != '√')
                	return null;
            	if (buf.length() > 0) {//버퍼에 숫자가 있으면 result로 보냄
                    result.add(buf.toString());
                    buf.delete(0, buf.length()+1);
                }
                buf.append(ch);// 버퍼 -> result로 보냄 
                result.add(buf.toString());
                buf.delete(0, buf.length()+1);
            }
        }
        if(!(buf.length()==0))
        	result.add(buf.toString());
        return result;
    }

    private static ArrayList<String> postfix(ArrayList<String> expr) {
        ArrayList<String> result = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();
        Stack<String> Rstack = new Stack<String>();
        int inLeft = 0;
        //if(getType(result.get(result.size())) == NUM)
        for (String str : expr) {
            if (getType(str) == NUM){
                result.add(str);
            }else if (getType(str) == LEFT) {
            	inLeft = 1;
                stack.push(str);
            }else if (getType(str) == ROOT) {
                Rstack.push(str);
            } else if (getType(str) == OP) {
            	if (!Rstack.isEmpty()&&(inLeft == 0))
            		result.add(Rstack.pop());
                if (stack.isEmpty())
                    stack.push(str);
                else {
                    while (!stack.isEmpty()) {
                        if (getPrecedence(stack.lastElement()) >= getPrecedence(str))
                            result.add(stack.pop());
                        else
                            break;
                    }
                    stack.push(str);
                }
            } else if (getType(str) == RIGHT) {
                while (!stack.isEmpty() && (getType(stack.lastElement()) != LEFT)) {
                    result.add(stack.pop());
                }
                inLeft = 0;
                if(!Rstack.isEmpty())
                	result.add(Rstack.pop());
                stack.pop();
            }
        }
        while (!Rstack.isEmpty()) {
            result.add(Rstack.pop());
        }
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        
        return result;
    }
    private static String executionINT(ArrayList<String> expr) throws Exception{
        Stack<Long> stack = new Stack<Long>();
        long n1, n2, result;

        for (String str : expr) {
            if (getType(str) == NUM) {
                stack.push(Long.parseLong(str));
            } else if (getType(str) == ROOT) {
            	n1 = (long)stack.pop();
            	stack.push((long) Math.sqrt(n1));
            } else if (getType(str) == OP) {
            
                n2 = (long)stack.pop();
                n1 = (long)stack.pop();
                if (str.equals("+")) {
                    result = n1 + n2;
                } else if (str.equals("-")) {
                    result = n1 - n2;
                } else if (str.equals("*")) {
                    result = n1 * n2;
                } else if (str.equals("/")) {// 나머지 확인해야함 
                    if (n2 == 0)
                        return null;
                    else
                        result = n1 / n2;
                } else if (str.equals("^")) {
                    result = (int) Math.pow(n1, n2);
                } else if (str.equals("<")) {
                    result = (int) (n1 * Math.pow(2, n2));
                } else if (str.equals(">")) {
                    result = (int) (n1 / Math.pow(2, n2));//확인해야함
                } else if (str.equals("|")) {
                    result = n1 | n2;
                } else if (str.equals("&")) {
                    result = n1 & n2;
                } else {
                    return null;
                }
                stack.push(result);
            }
        }
        return stack.pop().toString();
    }
    private static String executionDOUBLE(ArrayList<String> expr) throws Exception {
        Stack<Double> stack = new Stack<Double>();
        double n1, n2, result;
     
        for (String str : expr) {
            if (getType(str) == NUM) {
                stack.push(Double.parseDouble(str));
            } else if (getType(str) == ROOT) {
            	n1 = stack.pop();
            	stack.push(Math.sqrt(n1));
            }else if (getType(str) == OP) {
                n2 = stack.pop();
                n1 = stack.pop();
                if (str.equals("+")) {
                    result = n1 + n2;
                } else if (str.equals("-")) {
                    result = n1 - n2;
                } else if (str.equals("*")) {
                    result = n1 * n2;
                } else if (str.equals("/")) {// 나머지 확인해야함 
                    if (n2 == 0)
                        return null;
                    else
                        result = n1 / n2;
                } else if (str.equals("^")) {
                    result =  Math.pow(n1, n2);
                } else {
                    return null;
                }
                stack.push(result);
            }
        }
        return String.format("%.2f",Double.parseDouble(stack.pop().toString()));
    }
    //TODO Method 정수, 실수용 각각 분리
    public static String calcInt(String expr) throws Exception {
        ArrayList<String> parsed_expr;
        String result = null;
        if(expr.length() <= 0)
            return result;
        parsed_expr = CalUtil.parse(expr);
        if (parsed_expr != null) {
            result = CalUtil.executionINT(CalUtil.postfix(parsed_expr));
        }
        return result;
    }
    public static String calcDouble(String expr) throws Exception {
        ArrayList<String> parsed_expr;
        String result = null;
        if(expr.length() <= 0)
        	return result;
        parsed_expr = CalUtil.parse(expr);
        if (parsed_expr != null) {
        		result = CalUtil.executionDOUBLE(CalUtil.postfix(parsed_expr));
        }
        return result;
    }

}
