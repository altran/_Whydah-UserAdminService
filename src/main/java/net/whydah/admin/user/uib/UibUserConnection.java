package net.whydah.admin.user.uib;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.config.AppConfig;
import net.whydah.admin.user.ConflictExeption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by baardl on 17.04.14.
 */
@Component
public class UibUserConnection {
    private static final Logger log = LoggerFactory.getLogger(UibUserConnection.class);
    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();
    private static final int STATUS_FORBIDDEN = 403;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_CONFLICT = 409;
    private static final int STATUS_NO_CONTENT = 204;


    private final WebTarget uib;

    @Autowired
    public UibUserConnection(AppConfig appConfig) {
        Client client = ClientBuilder.newClient();
//        URI useridbackendUri = URI.create(appConfig.getProperty("userIdentityBackendUri"));
        // uib = client.target(userIdentityBackendUri);
        String uibUrl = appConfig.getProperty("useridentitybackend");
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uib = client.target(uibUrl);
    }

    public UserAggregate addUserAgregate(String userAdminServiceTokenId, String userTokenId, String userAggregateJson) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/user");
        UserAggregate userAggregate = null;
        UserAggregateRepresentation userAggregateRepresentation = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userAggregateJson, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("Response from UIB {}", response.readEntity(String.class));
                userAggregateRepresentation = UserAggregateRepresentation.fromJson(userAggregateJson);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + userAggregateJson + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        userAggregate = userAggregateRepresentation.getUserAggregate();
        return userAggregate;
    }

    public UserIdentity createUser(String userAdminServiceTokenId, String userTokenId, String userIdentityJson) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/user");
        UserIdentity userIdentity = null;
        UserAggregateRepresentation userAggregateRepresentation = null;
       // userIdentityJson = "{\"username\":\"per\",\"firstName\":\"per\",\"lastName\":\"per\",\"email\":\"per.per@example.com\",\"cellPhone\":\"123456789\",\"personRef\":\"ref\"}";
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userIdentityJson, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        String userJson = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("createUser-Response from UIB {}", userJson);
                userIdentity = UserIdentity.fromJson(userJson);
                break;
            case STATUS_CREATED:
                log.trace("createUser-userCreated {}", userJson);
                userIdentity = UserIdentity.fromJson(userJson);
                break;
            case STATUS_CONFLICT:
                log.info("Duplicate creation of user attempted on {}", userIdentityJson);
                throw new ConflictExeption("DuplicateCreateAttempted on " + userIdentityJson);
            case STATUS_BAD_REQUEST:
                log.error("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
                throw new BadRequestException("BadRequest for Json " + userIdentityJson + ",  Status code " + response.getStatus());
            default:
                log.error("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return userIdentity;
    }

    public boolean changePassword(String userAdminServiceTokenId, String adminUserTokenId, String userName, String password) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(userName).path("changepassword");
        boolean updatedOk = false;
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(password, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        String passwordJson = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("changePassword-Response from UIB {}", passwordJson);
                updatedOk = true;
                break;
            case STATUS_FORBIDDEN:
                log.error("changePassword-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), passwordJson);
                break;
            default:
                log.error("changePassword-Response from UIB: {}: {}", response.getStatus(), passwordJson);
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return updatedOk;
    }

    public RoleRepresentation addRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, RoleRepresentationRequest roleRequest) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role");
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(roleRequest.toJson(), MediaType.APPLICATION_JSON));
        String roleJson = response.readEntity(String.class);
        RoleRepresentation role = null;
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_OK:
                log.trace("addRole-Response from UIB {}", roleJson);
                role = RoleRepresentation.fromJson(roleJson);
                break;
            case STATUS_CREATED:
                log.trace("addRole-roleCreated {}", roleJson);
                role = RoleRepresentation.fromJson(roleJson);
                break;
            case STATUS_CONFLICT:
                log.info("Duplicate creation of role attempted on {}", roleJson);
                throw new ConflictExeption("DuplicateCreateAttempted on " + roleJson);
            case STATUS_BAD_REQUEST:
                log.error("addRole-Response from UIB: {}: {}",statusCode, roleJson);
                throw new BadRequestException("BadRequest for Json " + roleJson + ",  Status code " + statusCode);
            default:
                log.error("addRole-Response from UIB: {}: {}", statusCode, roleJson);
                throw new AuthenticationFailedException("Authentication failed. Status code " + statusCode);
        }
        return role;

    }

    public void deleteUserRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, String userRoleId) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role").path(userRoleId);
        Response response = webResource.request(MediaType.APPLICATION_JSON).delete();
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_NO_CONTENT:
                log.trace("deleteUserRole-Response from UIB {}", userRoleId);
                break;
            case STATUS_BAD_REQUEST:
                log.error("deleteUserRole-Response from UIB: {}: {}",statusCode, userRoleId);
                throw new BadRequestException("deleteUserRole for userRoleId " + userRoleId + ",  Status code " + statusCode);
            default:
                log.error("deleteUserRole-Response from UIB: {}: {}", statusCode, userRoleId);
                throw new RuntimeException("DeleteUserRole failed. Status code " + statusCode);
        }

    }



    public UserAggregate addPropertyOrRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserPropertyAndRole userPropertyAndRole) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role");
        UserAggregate updatedUser = null;
        UserAggregateRepresentation userAggregateRepresentation = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userPropertyAndRole.toJson(), MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("addPropertyOrRole-Response from UIB {}", response.readEntity(String.class));
                userAggregateRepresentation = UserAggregateRepresentation.fromJson(response.readEntity(String.class));
                if (userAggregateRepresentation != null) {
                    updatedUser = userAggregateRepresentation.getUserAggregate();
                }
                break;
            case STATUS_FORBIDDEN:
                log.error("addPropertyOrRole-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), response.readEntity(String.class));
                break;
            default:
                log.error("addPropertyOrRole-Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("addPropertyOrRole failed. Status code " + response.getStatus());
        }
        return updatedUser;
    }


    //TODO Clean up exception/failure handling
    public UserIdentity getUserIdentity(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).get();
        String responseBody = response.readEntity(String.class);
        switch (response.getStatus()) {
            case STATUS_OK:
                log.trace("getUserIdentity-Response from Uib {}", responseBody);
                UserIdentity userIdentity = UserIdentity.fromJson(responseBody);
                return userIdentity;
            case STATUS_FORBIDDEN:
                log.error("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                return null;
            default:
                log.error("getUserIdentity-Response from UIB: {}: {}", response.getStatus(), responseBody);
                throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
        }

        /*
        UserAggregate userAggregate = null;
        UserAggregateRepresentation userAggregateRepresentation;
        switch (statusCode) {
            case STATUS_OK:
                log.trace("getUserIdentity-Response from Uib {}", responseBody);
                userAggregateRepresentation = UserAggregateRepresentation.fromJson(responseBody);
                if (userAggregateRepresentation != null) {
                    userAggregate = userAggregateRepresentation.getUserAggregate();
                }
                break;
            case STATUS_FORBIDDEN:
                log.error("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                break;
            default:
                log.error("getUserIdentity-Response from UIB: {}: {}", response.getStatus(), responseBody);
                throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
        }
        return userAggregate;
        */
    }

    public UserAggregate getUserAggregateByUid(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("useraggregate").path(uid);
        UserAggregate userAggregate = null;
        UserAggregateRepresentation userAggregateRepresentation;
        Response response = webResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        String responseBody = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("getUserAggregateByUid-Response from Uib {}", responseBody);
                userAggregateRepresentation = UserAggregateRepresentation.fromJson(responseBody);
                if (userAggregateRepresentation != null) {
                    userAggregate = userAggregateRepresentation.getUserAggregate();
                }
                break;
            case STATUS_FORBIDDEN:
                log.error("getUserAggregateByUid-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                break;
            default:
                log.error("getUserAggregateByUid-Response from UIB: {}: {}", response.getStatus(), responseBody);
                throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
        }
        return userAggregate;
    }


    public String getRolesAsJson(String userAdminServiceTokenId, String userTokenId, String uid) {
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("/user").path(uid).path("roles");
        Response response = webResource.request(MediaType.APPLICATION_JSON).get();
        return findResponseBody("getRolesAsJson", response);
    }

    private String findResponseBody(String methodName, Response response) {
        String responseBody = null;
        int statusCode = response.getStatus();
        responseBody = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("{}-Response from UIB {}", methodName,responseBody);
                break;
            case STATUS_FORBIDDEN:
                log.error("{}-Not allowed from UIB: {}: {} ", methodName,response.getStatus(), responseBody);
                responseBody = null;
                break;
            default:
                log.error("{}-Response from UIB: {}: {}", methodName,response.getStatus(), responseBody);
                throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
        }
        return responseBody;
    }


    public void deleteUser(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).delete();
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_NO_CONTENT:
                log.trace("deleteUser-Response from UIB uid={}", uid);
                break;
            case STATUS_BAD_REQUEST:
                log.error("deleteUser-Response from UIB: {}: uid={}", statusCode, uid);
                throw new BadRequestException("deleteUserRole for uid=" + uid + ",  Status code " + statusCode);
            default:
                log.error("deleteUser-Response from UIB: {}, uid=", statusCode, uid);
                throw new RuntimeException("DeleteUser failed. Status code " + statusCode);
        }
    }
}
