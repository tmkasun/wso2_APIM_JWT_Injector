package org.wso2.carbon.custom.jwt.handler;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class AlterJWTHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(AlterJWTHandler.class);

    private static final String CUSTOM_HEADER_NAME = "x-myKey";
    private static final String JWT_HEADER_NAME = "X-JWT-Assertion";

    public boolean handleRequest(MessageContext messageContext) {
        Instant start = Instant.now();
        boolean result = alterJWT(messageContext);
        Instant end = Instant.now();
        log.info("Time taken to handleRequest in AlterJWTHandler = "+ Duration.between(start, end));
        return result;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    private boolean alterJWT(MessageContext synCtx) {
        Map headers = getTransportHeaders(synCtx);
        String myKey = getKeyHeader(headers);
        if (myKey == null) {
            log.warn("Can't find the " + CUSTOM_HEADER_NAME + " header in the API Request");
            return true;
        }
        String jwtHeader = getJWTHeader(headers);
        if (jwtHeader == null) {
            log.warn("Can't find the " + JWT_HEADER_NAME + " header in the API Request");
            return true;
        }

        try {
            SignedJWT idToken = SignedJWT.parse(jwtHeader);
            Payload pl = idToken.getPayload();
            JSONObject jsonPl = pl.toJSONObject();
            jsonPl.put(CUSTOM_HEADER_NAME, myKey);
            JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder(JWTClaimsSet.parse(jsonPl));
            KeyStoreManager keyStoreManager;
            keyStoreManager = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
            keyStoreManager.getDefaultPrimaryCertificate();
            JWSSigner signer = new RSASSASigner(keyStoreManager.getDefaultPrivateKey());
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), jwtClaimsSetBuilder.build());

            Instant start = Instant.now();
            signedJWT.sign(signer);
            Instant end = Instant.now();
            log.info("Time taken to sign the new JWT = "+ Duration.between(start, end));

            String newJwtString = signedJWT.serialize();
            setJWTHeader(synCtx, newJwtString);
            log.info(newJwtString);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getJWTHeader(Map headers) {
        return (String) headers.get(JWT_HEADER_NAME);
    }

    private String getKeyHeader(Map headers) {
        return (String) headers.get(CUSTOM_HEADER_NAME);
    }

    private Map getTransportHeaders(MessageContext messageContext) {
        return (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    private void setJWTHeader(MessageContext messageContext, String value) {
        getTransportHeaders(messageContext).put(JWT_HEADER_NAME, value);
    }

}
