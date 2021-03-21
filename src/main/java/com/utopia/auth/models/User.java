package com.utopia.auth.models;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Integer userId;

	@NotNull(message = "First name should not be empty")
	@Column(name = "first_name")
	private String userFirstName;

	@NotNull(message = "Last name should not be empty")
	@Column(name = "last_name")
	private String userLastName;

	@NotNull(message = "Email should not be empty")
	@Column(name = "email")
	private String userEmail;

	@NotNull(message = "Password should not be empty")
	@Column(name = "password")
	private String userPassword;

	@NotNull(message = "Phone number should not be empty")
	@Column(name = "phone")
	private String userPhone;

	@NotNull(message = "Role should not be empty")
	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private Role userRole;

	@Transient
	private String userToken;

	public User() {}
	public User(Role userRole, String userFirstName, String userLastName, String userEmail, String userPassword, String userPhone) {

		this.userRole = userRole;
		this.userFirstName = userFirstName;
		this.userLastName = userLastName;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
		this.userPhone = userPhone;
	}

	public User(Role userRole, String userFirstName, String userLastName, String userEmail, String userPassword, String userPhone,
			String userToken) {

		this.userFirstName = userFirstName;
		this.userLastName = userLastName;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
		this.userPhone = userPhone;
		this.userRole = userRole;
		this.userToken = userToken;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserFirstName() {
		return this.userFirstName;
	}

	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}

	public String getUserLastName() {
		return this.userLastName;
	}

	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserPassword() {
		return this.userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserPhone() {
		return this.userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public Role getUserRole() {
		return this.userRole;
	}

	public void setUserRole(Role userRole) {
		this.userRole = userRole;
	}


	public String getUserToken() {
		return this.userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
}
