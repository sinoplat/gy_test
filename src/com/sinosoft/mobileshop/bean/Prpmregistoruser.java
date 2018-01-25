package com.sinosoft.mobileshop.bean;

import java.util.Date;

public class Prpmregistoruser {
	private static final long serialVersionUID = 1L;

	/** 属性员工代码 */
	private String userCode;

	/** 属性imei */
	private String imei;
	
	/** 属性registphoneno */
	private String registphoneno;
	
	/** 属性registusername */
	private String registusername;

	/** 属性registtime */
	private String registtime;

	/** 属性registpassword */
	private String registpassword;

	/** 属性errortimes */
	private Integer errortimes;

	/** 属性lastlogintime */
	private Date lastlogintime;

	/** 属性lastregisttime */
	private Date lastregisttime;

	/** 属性locktime */
	private Date locktime;

	/** 属性员工名称 */
	private String userName;

	/** 属性机构代码 */
	private String comCode;

	/** 属性标志 */
	private String flag;

	/** 属性备注 */
	private String remark;
	
	/** 属性用户邮箱 */
	private String email;
	
	/** 属性是否有效*/
	private String validstatus;
	
	/** 属性邮件是否发送成功：0-失败，1-成功 */
	private String emailSendFlag;
	
	/** 属性验证是否通过：0-未通过，1-已通过 */
	private String validPassFlag;
	
	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getRegistphoneno() {
		return registphoneno;
	}

	public void setRegistphoneno(String registphoneno) {
		this.registphoneno = registphoneno;
	}

	public String getRegistusername() {
		return registusername;
	}

	public void setRegistusername(String registusername) {
		this.registusername = registusername;
	}

	public String getRegisttime() {
		return registtime;
	}

	public void setRegisttime(String registtime) {
		this.registtime = registtime;
	}

	public String getRegistpassword() {
		return registpassword;
	}

	public void setRegistpassword(String registpassword) {
		this.registpassword = registpassword;
	}

	public Integer getErrortimes() {
		return errortimes;
	}

	public void setErrortimes(Integer errortimes) {
		this.errortimes = errortimes;
	}

	public Date getLastlogintime() {
		return lastlogintime;
	}

	public void setLastlogintime(Date lastlogintime) {
		this.lastlogintime = lastlogintime;
	}

	public Date getLastregisttime() {
		return lastregisttime;
	}

	public void setLastregisttime(Date lastregisttime) {
		this.lastregisttime = lastregisttime;
	}

	public Date getLocktime() {
		return locktime;
	}

	public void setLocktime(Date locktime) {
		this.locktime = locktime;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getComCode() {
		return comCode;
	}

	public void setComCode(String comCode) {
		this.comCode = comCode;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getValidstatus() {
		return validstatus;
	}

	public void setValidstatus(String validstatus) {
		this.validstatus = validstatus;
	}

	public String getEmailSendFlag() {
		return emailSendFlag;
	}

	public void setEmailSendFlag(String emailSendFlag) {
		this.emailSendFlag = emailSendFlag;
	}

	public String getValidPassFlag() {
		return validPassFlag;
	}

	public void setValidPassFlag(String validPassFlag) {
		this.validPassFlag = validPassFlag;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
