package kr.ac.ajou.esd.calculator;

import java.io.Serializable;
import java.util.UUID;

/**
 * 
 * History Row 의 정보를 갖고 있는 Value object
 * 
 * 수정은 불가능 하도록 getter 만 생성.
 *
 */
public class HistoryVO implements Serializable{
	private static final long serialVersionUID = -6455604147419455000L;

	public HistoryVO(String expression, String result) {
		this.uuid = UUID.randomUUID().toString(); //계산결과 최초 생성시 UUID 를 생성해서 키값 관리.  
		this.expression = expression;
		this.result = result;
	}

	public HistoryVO(String uuid, String expression, String result) {
		this.uuid = uuid;
		this.expression = expression;
		this.result = result;
	}

	private String uuid;
	private String expression;
	private String result;
	
	public String getUuid() {
		return uuid;
	}
	public String getExpression() {
		return expression;
	}
	public String getResult() {
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("HistoryVO{");
		sb.append("uuid='").append(uuid).append('\'');
		sb.append(", expression='").append(expression).append('\'');
		sb.append(", result='").append(result).append('\'');
		sb.append('}');
		return sb.toString();
	}
}