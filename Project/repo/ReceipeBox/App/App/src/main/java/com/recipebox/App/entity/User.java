package com.recipebox.App.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.recipebox.App.converter.UserRoleConverter;
import com.recipebox.App.enums.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

	// data

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private int userId;

    @NotBlank(message = "Name cannot be left blank") 
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email format") 
    @Column(nullable = false, unique = true, length = 100)
    private String email;

//    @NotBlank(message = "Password is required")
//    @Size(min = 6, message = "Password must be at least 6 characters long") 
    @Column(nullable = false, length = 255)
    private String password_hash;

	@Convert(converter = UserRoleConverter.class)
	private UserRole role;

	private Boolean isActive = true;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UserAllergy> userAllergies = new ArrayList<>();

    public List<UserAllergy> getUserAllergies() {
        return userAllergies;
    }

    public void setUserAllergies(List<UserAllergy> userAllergies) {
        this.userAllergies = userAllergies;
    }

	///// constructor/////////
	// ✅ FIXED:
	public User(int userId, String name, String email, String password_hash, UserRole role, Boolean isActive,
	        LocalDateTime createdAt) {
	    super();
	    this.userId = userId;
	    this.name = name;
	    this.email = email;
	    this.password_hash = password_hash;
	    this.role = role;
	    this.isActive = isActive;
	    this.createdAt = createdAt;
	}

	public User() {
		super();
		// TODO Auto-generated constructor stub
	}

//	private Set<UserAllergies> allergies = new HashSet<UserAllergies>();

	////////// get set////////////
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword_hash() {
		return password_hash;
	}

	public void setPassword_hash(String password_hash) {
		this.password_hash = password_hash;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
