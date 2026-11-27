package org.example.buffet.entity;


public class User {
    private Long id;
    private String username;
    private String passwdHash;
    private String displayName;
    private Role role;
    private boolean enabled;

    public User(){

    }

    public User(Long id, String username, String passwdHash, String displayName, Role role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.passwdHash = passwdHash;
        this.displayName = displayName;
        this.role = role;
        this.enabled = enabled;
    }

    public String getPasswdHash() {
        return passwdHash;
    }

    public void setPasswdHash(String passwdHash) {
        this.passwdHash = passwdHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
