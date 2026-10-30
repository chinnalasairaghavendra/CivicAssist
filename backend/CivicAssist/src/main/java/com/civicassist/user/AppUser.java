package com.civicassist.user;


import com.civicassist.department.Department;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name="users")
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,length=100)
    private String name;

    @Column(nullable=false,unique = true,length=254)
    private String email;

    @Column(name="password_hash",nullable=false,length=100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name="created_at",nullable=false,updatable=false)
    private OffsetDateTime createdAt;

    protected AppUser(){

    }

    public AppUser(String name,String email,String passwordHash){
        this.name=name;
        this.email=email;
        this.passwordHash=passwordHash;
        this.role=UserRole.CITIZEN;
    }

    @PrePersist
    void onCreate(){
        if(createdAt==null)
            createdAt=OffsetDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(
                new SimpleGrantedAuthority("ROLE_"+role.name())
        );
    }

    @Override
    public String getUsername(){
        return email;
    }

    @Override
    public String getPassword(){
        return passwordHash;
    }
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }

    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }
    public UserRole getRole(){
        return role;
    }

    public OffsetDateTime getCreatedAt(){
        return createdAt;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
