/**
 * This program is licensed under the terms of the eBay Common Development and
 * Distribution License (CDDL) Version 1.0 (the "License") and any subsequent
 * version thereof released by eBay.  The then-current version of the License
 * can be found at http://www.opensource.org/licenses/cddl1.php
 */

package com.ebay.api.soap.examples;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.SOAPEnvelope;

public class ClientCredentialHandler extends BasicHandler {
  private static final String ApiNamespace = "urn:ebay:apis:eBLBaseComponents";
  private static final String EBLPrefix = "ebl";
  private static final String EBLNamespace = "urn:ebay:apis:eBLBaseComponents";
  private static String privAppId = "<Dummy App Id>";
  private static String privDevId = "<Dummy Dev Id>";
  private static String privAuthCert = "<Dummy Auth Id>";
  private static String privUsername = null;
  private static String privPassword = null;
  private static String privToken = null;

  public void invoke(MessageContext msgContext) throws AxisFault {
  	Message req = msgContext.getRequestMessage();
    setSecurityHeader(req.getSOAPEnvelope());
  }

  private void setSecurityHeader(SOAPEnvelope env) throws AxisFault {
    try {
      SOAPHeader envHeader = env.getHeader();

      SOAPElement reqCred = envHeader.addChildElement("RequesterCredentials", "api", ApiNamespace);
      ((SOAPHeaderElement) reqCred).setMustUnderstand(false);
      reqCred.addNamespaceDeclaration(EBLPrefix, EBLNamespace);
      if (getToken() != null) {
        reqCred.addChildElement("eBayAuthToken",
            EBLPrefix).addTextNode(getToken());
      }

      SOAPElement credNode = reqCred.addChildElement("Credentials", EBLPrefix);
      credNode.addChildElement("AppId", EBLPrefix).addTextNode(getAppId());
      credNode.addChildElement("DevId", EBLPrefix).addTextNode(getDevId());
      credNode.addChildElement("AuthCert", EBLPrefix).addTextNode(getAuthCert());
      if (getUsername() != null) {
        credNode.addChildElement("Username", EBLPrefix).addTextNode(getUsername());
      }
      if (getPassword() != null) {
        credNode.addChildElement("Password", EBLPrefix).addTextNode(getPassword());
      }
    }
    catch (Exception ex) {
      throw new AxisFault(ex.getMessage());
    }

  }

  /**
   * getAppId
   *
   * @return String
   */
  public static String getAppId() {
    return privAppId;
  }

  /**
   * getDevId
   *
   * @return String
   */
  public static String getDevId() {
    return privDevId;
  }

  /**
   * getAuthCert
   *
   * @return String
   */
  public static String getAuthCert() {
    return privAuthCert;
  }

  /**
   * getUsernameId
   *
   * @return String
   */
  public static String getUsername() {
    return privUsername;
  }

  /**
   * getPassword
   *
   * @return String
   */
  public static String getPassword() {
    return privPassword;
  }

  /**
   * getToken
   *
   * @return String
   */
  public static String getToken() {
    return privToken;
  }

  /**
   * setDevId
   *
   * @param newDevId String
   */
  public static void setDevId(String newDevId) {
    privDevId = newDevId;
  }

  /**
   * setAppId
   *
   * @param newAppId String
   */
  public static void setAppId(String newAppId) {
    privAppId = newAppId;
  }

  /**
   * setAuthCert
   *
   * @param newAuthCert String
   */
  public static void setAuthCert(String newAuthCert) {
    privAuthCert = newAuthCert;
  }

  /**
   * setUsername
   *
   * @param newUsername String
   */
  public static void setUsername(String newUsername) {
    privUsername = newUsername;
  }

  /**
   * setPassword
   *
   * @param newPassword String
   */
  public static void setPassword(String newPassword) {
    privPassword = newPassword;
  }

  /**
   * setToken
   *
   * @param newToken String
   */
  public static void setToken(String newToken) {
    privToken = newToken;
  }

}

