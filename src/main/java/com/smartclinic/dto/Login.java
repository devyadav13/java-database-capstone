package com.smartclinic.dto;

/** Login payload. Doctors and patients log in with email; admins use the "identifier" as username. */
public class Login {
    private String identifier;
    private String password;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
