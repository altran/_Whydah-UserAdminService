package net.whydah.admin.user.uib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author totto
 */
public class UserIdentityRepresentation {
    private static final Logger log = LoggerFactory.getLogger(UserIdentityRepresentation.class);
    protected String username;
    protected String firstName;
    protected String lastName;
    protected String personRef;
    protected String email;
    protected String cellPhone;
    protected transient String password;

    public UserIdentityRepresentation() {
    }

    public UserIdentityRepresentation(String username) {
        this.username = username;
    }

    public UserIdentityRepresentation(String username, String firstName, String lastName, String personRef, String email, String cellPhone) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personRef = personRef;
        this.email = email;
        this.cellPhone = cellPhone;
    }

    public String getPersonRef() {
        return personRef;
    }
    public String getUsername() {
        return username;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    public String getCellPhone() {
        return cellPhone;
    }


    public void setPersonRef(String personRef) {
        this.personRef = personRef;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }


    public String toJsonBare() {
        String userJson = "{\"username\":\""+ username +"\",\"firstName\":\"" +firstName +"\",\"lastName\":\""+lastName+"\",\"personRef\":\""+personRef+
                "\",\"email\":\""+email+"\",\"cellPhone\":\""+cellPhone+"\"}";
        return userJson;
    }
}
