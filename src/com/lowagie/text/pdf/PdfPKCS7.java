/*
 * Copyright 2004 by Paulo Soares.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.math.BigInteger;
import com.lowagie.text.ExceptionConverter;

import com.lowagie.bc.asn1.ASN1InputStream;
import com.lowagie.bc.asn1.DERObject;
import com.lowagie.bc.asn1.ASN1Sequence;
import com.lowagie.bc.asn1.ASN1Set;
import com.lowagie.bc.asn1.DERObjectIdentifier;
import com.lowagie.bc.asn1.DEROctetString;
import com.lowagie.bc.asn1.DERTaggedObject;
import com.lowagie.bc.asn1.DERInteger;
import com.lowagie.bc.asn1.ASN1TaggedObject;
import com.lowagie.bc.asn1.DERConstructedSet;
import com.lowagie.bc.asn1.DERSequence;
import com.lowagie.bc.asn1.DERObjectIdentifier;
import com.lowagie.bc.asn1.DERNull;
import com.lowagie.bc.asn1.ASN1EncodableVector;
import com.lowagie.bc.asn1.DERNull;
import com.lowagie.bc.asn1.DERSet;
import com.lowagie.bc.asn1.DERString;
import com.lowagie.bc.asn1.ASN1OutputStream;

/**
 * This class does all the processing related to signing and verifying a PKCS#7
 * signature.
 * <p>
 * It's based in code found at org.bouncycastle.
 */
public class PdfPKCS7 {
    
    private int version, signerversion;
    private Set digestalgos;
    private Collection certs, crls;
    private X509Certificate signCert;
    private byte[] digest;
    private MessageDigest messageDigest;
    private String digestAlgorithm, digestEncryptionAlgorithm;
    private Signature sig;
    private transient PrivateKey privKey;
    private byte RSAdata[];
    private boolean verified;
    private boolean verifyResult;
    
    private static final String ID_PKCS7_DATA = "1.2.840.113549.1.7.1";
    private static final String ID_PKCS7_SIGNED_DATA = "1.2.840.113549.1.7.2";
    private static final String ID_MD5 = "1.2.840.113549.2.5";
    private static final String ID_MD2 = "1.2.840.113549.2.2";
    private static final String ID_SHA1 = "1.3.14.3.2.26";
    private static final String ID_RSA = "1.2.840.113549.1.1.1";
    private static final String ID_DSA = "1.2.840.10040.4.1";
    
    /**
     * Holds value of property reason.
     */
    private String reason;
    
    /**
     * Holds value of property location.
     */
    private String location;
    
    /**
     * Holds value of property signDate.
     */
    private Calendar signDate;
    
    /**
     * Holds value of property signName.
     */
    private String signName;
    
    /**
     * Verifies a signature using the sub-filter adbe.x509.rsa_sha1.
     * @param contentsKey the /Contents key
     * @param certsKey the /Cert key
     * @param provider the provider or <code>null</code> for the default provider
     * @throws SecurityException on error
     * @throws CRLException on error
     * @throws InvalidKeyException on error
     * @throws CertificateException on error
     * @throws NoSuchProviderException on error
     * @throws NoSuchAlgorithmException on error
     * @throws IOException on error
     */    
    public PdfPKCS7(byte[] contentsKey, byte[] certsKey, String provider) throws SecurityException, CRLException, InvalidKeyException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException, IOException {
        CertificateFactory cf;
        if (provider == null)
            cf = CertificateFactory.getInstance("X.509");
        else
            cf = CertificateFactory.getInstance("X.509", provider);
        if (provider == null)
            certs = cf.generateCertificates(new ByteArrayInputStream(certsKey));
        signCert = (X509Certificate)certs.iterator().next();
        crls = new ArrayList();
        ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(contentsKey));
        digest = ((DEROctetString)in.readObject()).getOctets();
        if (provider == null)
            sig = Signature.getInstance("SHA1withRSA");
        else
            sig = Signature.getInstance("SHA1withRSA", provider);
        sig.initVerify(signCert.getPublicKey());
    }
    
    /**
     * Verifies a signature using the sub-filter adbe.pkcs7.detached or
     * adbe.pkcs7.sha1.
     * @param contentsKey the /Contents key
     * @param provider the provider or <code>null</code> for the default provider
     * @throws SecurityException on error
     * @throws CRLException on error
     * @throws InvalidKeyException on error
     * @throws CertificateException on error
     * @throws NoSuchProviderException on error
     * @throws NoSuchAlgorithmException on error
     */    
    public PdfPKCS7(byte[] contentsKey, String provider) throws SecurityException, CRLException, InvalidKeyException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException {
        ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(contentsKey));
        
        //
        // Basic checks to make sure it's a PKCS#7 SignedData Object
        //
        DERObject pkcs;
        
        try {
            pkcs = din.readObject();
        }
        catch (IOException e) {
            throw new SecurityException("can't decode PKCS7SignedData object");
        }
        if (!(pkcs instanceof ASN1Sequence)) {
            throw new SecurityException("Not a valid PKCS#7 object - not a sequence");
        }
        ASN1Sequence signedData = (ASN1Sequence)pkcs;
        DERObjectIdentifier objId = (DERObjectIdentifier)signedData.getObjectAt(0);
        if (!objId.getId().equals(ID_PKCS7_SIGNED_DATA))
            throw new SecurityException("Not a valid PKCS#7 object - not signed data");
        ASN1Sequence content = (ASN1Sequence)((DERTaggedObject)signedData.getObjectAt(1)).getObject();
        // the positions that we care are:
        //     0 - version
        //     1 - digestAlgorithms
        //     2 - possible ID_PKCS7_DATA
        //     (the certificates and crls are taken out by other means)
        //     last - signerInfos
        
        // the version
        version = ((DERInteger)content.getObjectAt(0)).getValue().intValue();
        
        // the digestAlgorithms
        digestalgos = new HashSet();
        Enumeration e = ((ASN1Set)content.getObjectAt(1)).getObjects();
        while (e.hasMoreElements())
        {
            ASN1Sequence s = (ASN1Sequence)e.nextElement();
            DERObjectIdentifier o = (DERObjectIdentifier)s.getObjectAt(0);
            digestalgos.add(o.getId());
        }
        
        // the certificates and crls
        CertificateFactory cf;
        if (provider == null)
            cf = CertificateFactory.getInstance("X.509");
        else
            cf = CertificateFactory.getInstance("X.509", provider);
        certs = cf.generateCertificates(new ByteArrayInputStream(contentsKey));
        crls = cf.generateCRLs(new ByteArrayInputStream(contentsKey));
        
        // the possible ID_PKCS7_DATA
        ASN1Sequence rsaData = (ASN1Sequence)content.getObjectAt(2);
        if (rsaData.size() > 1) {
            DEROctetString rsaDataContent = (DEROctetString)((DERTaggedObject)rsaData.getObjectAt(1)).getObject();
            RSAdata = rsaDataContent.getOctets();
        }
        
        // the signerInfos
        int next = 3;
        while (content.getObjectAt(next) instanceof DERTaggedObject)
            ++next;
        ASN1Set signerInfos = (ASN1Set)content.getObjectAt(next);
        if (signerInfos.size() != 1)
            throw new SecurityException("This PKCS#7 object has multiple SignerInfos - only one is supported at this time");
        ASN1Sequence signerInfo = (ASN1Sequence)signerInfos.getObjectAt(0);
        // the positions that we care are
        //     0 - version
        //     1 - the signing certificate serial number
        //     2 - the digest algorithm
        //     3 or 4 - digestEncryptionAlgorithm
        //     4 or 5 - encryptedDigest
        signerversion = ((DERInteger)signerInfo.getObjectAt(0)).getValue().intValue();
        // Get the signing certificate
        ASN1Sequence issuerAndSerialNumber = (ASN1Sequence)signerInfo.getObjectAt(1);
        BigInteger serialNumber = ((DERInteger)issuerAndSerialNumber.getObjectAt(1)).getValue();
        for (Iterator i = certs.iterator(); i.hasNext();) {
            X509Certificate cert = (X509Certificate)i.next();
            if (serialNumber.equals(cert.getSerialNumber())) {
                signCert = cert;
                break;
            }
        }
        if (signCert == null) {
            throw new SecurityException("Can't find signing certificate with serial " + serialNumber.toString(16));
        }
        digestAlgorithm = ((DERObjectIdentifier)((ASN1Sequence)signerInfo.getObjectAt(2)).getObjectAt(0)).getId();
        next = 3;
        if (signerInfo.getObjectAt(next) instanceof ASN1TaggedObject)
            ++next;
        digestEncryptionAlgorithm = ((DERObjectIdentifier)((ASN1Sequence)signerInfo.getObjectAt(next++)).getObjectAt(0)).getId();
        digest = ((DEROctetString)signerInfo.getObjectAt(next)).getOctets();
        if (RSAdata != null) {
            if (provider == null)
                messageDigest = MessageDigest.getInstance(getHashAlgorithm());
            else
                messageDigest = MessageDigest.getInstance(getHashAlgorithm(), provider);
        }
        if (provider == null)
            sig = Signature.getInstance(getDigestAlgorithm());
        else
            sig = Signature.getInstance(getDigestAlgorithm(), provider);
        sig.initVerify(signCert.getPublicKey());
    }

    /**
     * Generates a signature.
     * @param privKey the private key
     * @param certChain the certificate chain
     * @param crlList the certificate revocation list
     * @param hashAlgorithm the hash algorithm
     * @param provider the provider or <code>null</code> for the default provider
     * @param hasRSAdata <CODE>true</CODE> if the sub-filter is adbe.pkcs7.sha1
     * @throws SecurityException on error
     * @throws InvalidKeyException on error
     * @throws NoSuchProviderException on error
     * @throws NoSuchAlgorithmException on error
     */    
    public PdfPKCS7(PrivateKey privKey, Certificate[] certChain, CRL[] crlList,
                    String hashAlgorithm, String provider, boolean hasRSAdata)
      throws SecurityException, InvalidKeyException, NoSuchProviderException,
      NoSuchAlgorithmException
    {
        this.privKey = privKey;
        
        if (hashAlgorithm.equals("MD5")) {
            digestAlgorithm = ID_MD5;
        }
        else if (hashAlgorithm.equals("MD2")) {
            digestAlgorithm = ID_MD2;
        }
        else if (hashAlgorithm.equals("SHA")) {
            digestAlgorithm = ID_SHA1;
        }
        else if (hashAlgorithm.equals("SHA1")) {
            digestAlgorithm = ID_SHA1;
        }
        else {
            throw new NoSuchAlgorithmException("Unknown Hash Algorithm "+hashAlgorithm);
        }
        
        version = signerversion = 1;
        certs = new ArrayList();
        crls = new ArrayList();
        digestalgos = new HashSet();
        digestalgos.add(digestAlgorithm);
        
        //
        // Copy in the certificates and crls used to sign the private key.
        //
        signCert = (X509Certificate)certChain[0];
        for (int i = 0;i < certChain.length;i++) {
            certs.add(certChain[i]);
        }
        
        if (crlList != null) {
            for (int i = 0;i < crlList.length;i++) {
                crls.add(crlList[i]);
            }
        }
        
        //
        // Now we have private key, find out what the digestEncryptionAlgorithm is.
        //
        digestEncryptionAlgorithm = privKey.getAlgorithm();
        if (digestEncryptionAlgorithm.equals("RSA")) {
            digestEncryptionAlgorithm = ID_RSA;
        }
        else if (digestEncryptionAlgorithm.equals("DSA")) {
            digestEncryptionAlgorithm = ID_DSA;
        }
        else {
            throw new NoSuchAlgorithmException("Unknown Key Algorithm "+digestEncryptionAlgorithm);
        }
        if (hasRSAdata) {
            RSAdata = new byte[0];
            if (provider == null)
                messageDigest = MessageDigest.getInstance(getHashAlgorithm());
            else
                messageDigest = MessageDigest.getInstance(getHashAlgorithm(), provider);
        }

        if (provider == null)
            sig = Signature.getInstance(getDigestAlgorithm());
        else
            sig = Signature.getInstance(getDigestAlgorithm(), provider);
        
        sig.initSign(privKey);
    }

    /**
     * Update the digest with the specified bytes. This method is used both for signing and verifying
     * @param buf the data buffer
     * @param off the offset in the data buffer
     * @param len the data length
     * @throws SignatureException on error
     */
    public void update(byte[] buf, int off, int len) throws SignatureException {
        if (RSAdata != null)
            messageDigest.update(buf, off, len);
        else
            sig.update(buf, off, len);
    }
    
    /**
     * Verify the digest.
     * @throws SignatureException on error
     * @return <CODE>true</CODE> if the signature checks out, <CODE>false</CODE> otherwise
     */
    public boolean verify() throws SignatureException {
        if (verified)
            return verifyResult;
        if (RSAdata != null)
            sig.update(messageDigest.digest());
        verifyResult = sig.verify(digest);
        verified = true;
        return verifyResult;
    }
    
    /**
     * Get the X.509 certificates associated with this PKCS#7 object
     * @return the X.509 certificates associated with this PKCS#7 object
     */
    public Certificate[] getCertificates() {
        return (X509Certificate[])certs.toArray(new X509Certificate[0]);
    }
    
    /**
     * Get the X.509 certificate revocation lists associated with this PKCS#7 object
     * @return the X.509 certificate revocation lists associated with this PKCS#7 object
     */
    public Collection getCRLs() {
        return crls;
    }
    
    /**
     * Get the X.509 certificate actually used to sign the digest.
     * @return the X.509 certificate actually used to sign the digest
     */
    public X509Certificate getSigningCertificate() {
        return signCert;
    }
    
    /**
     * Get the version of the PKCS#7 object. Always 1
     * @return the version of the PKCS#7 object. Always 1
     */
    public int getVersion() {
        return version;
    }
    
    /**
     * Get the version of the PKCS#7 "SignerInfo" object. Always 1
     * @return the version of the PKCS#7 "SignerInfo" object. Always 1
     */
    public int getSigningInfoVersion() {
        return signerversion;
    }
    
    /**
     * Get the algorithm used to calculate the message digest
     * @return the algorithm used to calculate the message digest
     */
    public String getDigestAlgorithm() {
        String dea = digestEncryptionAlgorithm;
        
        if (digestEncryptionAlgorithm.equals(ID_RSA)) {
            dea = "RSA";
        }
        else if (digestEncryptionAlgorithm.equals(ID_DSA)) {
            dea = "DSA";
        }
        
        return getHashAlgorithm() + "with" + dea;
    }

    public String getHashAlgorithm() {
        String da = digestAlgorithm;
        
        if (digestAlgorithm.equals(ID_MD5)) {
            da = "MD5";
        }
        else if (digestAlgorithm.equals(ID_MD2)) {
            da = "MD2";
        }
        else if (digestAlgorithm.equals(ID_SHA1)) {
            da = "SHA1";
        }
        return da;
    }

    /**
     * Loads the default root certificates at &lt;java.home&gt;/lib/security/cacerts
     * with the default provider.
     * @return a <CODE>KeyStore</CODE>
     */    
    public static KeyStore loadCacertsKeyStore() {
        return loadCacertsKeyStore(null);
    }

    /**
     * Loads the default root certificates at &lt;java.home&gt;/lib/security/cacerts.
     * @param provider the provider or <code>null</code> for the default provider
     * @return a <CODE>KeyStore</CODE>
     */    
    public static KeyStore loadCacertsKeyStore(String provider) {
        File file = new File(System.getProperty("java.home"), "lib");
        file = new File(file, "security");
        file = new File(file, "cacerts");
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            KeyStore k;
            if (provider == null)
                k = KeyStore.getInstance("JKS");
            else
                k = KeyStore.getInstance("JKS", provider);
            k.load(fin, null);
            return k;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        finally {
            try{fin.close();}catch(Exception ex){}
        }
    }
    
    /**
     * Verifies a single certificate.
     * @param cert the certificate to verify
     * @param crls the certificate revocation list or <CODE>null</CODE>
     * @param calendar the date or <CODE>null</CODE> for the current date
     * @return a <CODE>String</CODE> with the error description or <CODE>null</CODE>
     * if no error
     */    
    public static String verifyCertificate(X509Certificate cert, Collection crls, Calendar calendar) {
        if (calendar == null)
            calendar = new GregorianCalendar();
        if (cert.hasUnsupportedCriticalExtension())
            return "Has unsupported critical extension";
        try {
            cert.checkValidity(calendar.getTime());
        }
        catch (Exception e) {
            return e.getMessage();
        }
        if (crls != null) {
            for (Iterator it = crls.iterator(); it.hasNext();) {
                if (((CRL)it.next()).isRevoked(cert))
                    return "Certificate revoked";
            }
        }
        return null;
    }
    
    /**
     * Verifies a certificate chain against a KeyStore.
     * @param certs the certificate chain
     * @param keystore the <CODE>KeyStore</CODE>
     * @param crls the certificate revocation list or <CODE>null</CODE>
     * @param calendar the date or <CODE>null</CODE> for the current date
     * @return <CODE>null</CODE> if the certificate chain could be validade or a
     * <CODE>Object[]{cert,error}</CODE> where <CODE>cert</CODE> is the
     * failed certificate and <CODE>error</CODE> is the error message
     */    
    public static Object[] verifyCertificates(Certificate certs[], KeyStore keystore, Collection crls, Calendar calendar) {
        if (calendar == null)
            calendar = new GregorianCalendar();
        ArrayList fails = new ArrayList();
        for (int k = 0; k < certs.length; ++k) {
            X509Certificate cert = (X509Certificate)certs[k];
            String err = verifyCertificate(cert, crls, calendar);
            if (err != null)
                return new Object[]{cert, err};
            try {
                for (Enumeration aliases = keystore.aliases(); aliases.hasMoreElements();) {
                    try {
                        String alias = (String)aliases.nextElement();
                        if (!keystore.isCertificateEntry(alias))
                            continue;
                        X509Certificate certStoreX509 = (X509Certificate)keystore.getCertificate(alias);
                        if (verifyCertificate(certStoreX509, crls, calendar) != null)
                            continue;
                        try {
                            cert.verify(certStoreX509.getPublicKey());
                            return null;
                        }
                        catch (Exception e) {
                            continue;
                        }
                    }
                    catch (Exception ex) {
                    }
                }
            }
            catch (Exception e) {
            }
            int j;
            for (j = 0; j < certs.length; ++j) {
                if (j == k)
                    continue;
                X509Certificate certNext = (X509Certificate)certs[j];
                try {
                    cert.verify(certNext.getPublicKey());
                    break;
                }
                catch (Exception e) {
                }
            }
            if (j == certs.length)
                return new Object[]{cert, "Cannot be verified against the KeyStore or the certificate chain"};
        }
        return new Object[]{null, "Invalid state. Possible circular certificate chain"};
    }

    /**
     * Get the "issuer" from the TBSCertificate bytes that are passed in
     */
    private static DERObject getIssuer(byte[] enc) {
        try {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(enc));
            ASN1Sequence seq = (ASN1Sequence)in.readObject();
            return (DERObject)seq.getObjectAt(seq.getObjectAt(0) instanceof DERTaggedObject ? 3 : 2);
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Get the "subject" from the TBSCertificate bytes that are passed in
     */
    private static DERObject getSubject(byte[] enc) {
        try {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(enc));
            ASN1Sequence seq = (ASN1Sequence)in.readObject();
            return (DERObject)seq.getObjectAt(seq.getObjectAt(0) instanceof DERTaggedObject ? 5 : 4);
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    public static X509Name getIssuerFields(X509Certificate cert) {
        try {
            return new X509Name((ASN1Sequence)getIssuer(cert.getTBSCertificate()));
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    public static X509Name getSubjectFields(X509Certificate cert) {
        try {
            return new X509Name((ASN1Sequence)getSubject(cert.getTBSCertificate()));
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    public byte[] getEncodedPKCS1() {
        try {
            digest = sig.sign();
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            
            ASN1OutputStream dout = new ASN1OutputStream(bOut);
            dout.writeObject(new DEROctetString(digest));
            dout.close();
            
            return bOut.toByteArray();
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    /**
     * return the bytes for the PKCS7SignedData object.
     */
    public byte[] getEncodedPKCS7() {
        try {
            if (RSAdata != null) {
                RSAdata = messageDigest.digest();
                sig.update(RSAdata);
            }
            digest = sig.sign();
            
            // Create the set of Hash algorithms
            DERConstructedSet digestAlgorithms = new DERConstructedSet();
            for(Iterator it = digestalgos.iterator(); it.hasNext();) {
                ASN1EncodableVector algos = new ASN1EncodableVector();
                algos.add(new DERObjectIdentifier((String)it.next()));
                algos.add(new DERNull());
                digestAlgorithms.addObject(new DERSequence(algos));
            }
            
            // Create the contentInfo.
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new DERObjectIdentifier(ID_PKCS7_DATA));
            if (RSAdata != null)
                v.add(new DERTaggedObject(0, new DEROctetString(RSAdata)));
            DERSequence contentinfo = new DERSequence(v);
            
            // Get all the certificates
            //
            v = new ASN1EncodableVector();
            for (Iterator i = certs.iterator(); i.hasNext();) {
                ASN1InputStream tempstream = new ASN1InputStream(new ByteArrayInputStream(((X509Certificate)i.next()).getEncoded()));
                v.add(tempstream.readObject());
            }
            
            DERSet dercertificates = new DERSet(v);
            
            // Create signerinfo structure.
            //
            ASN1EncodableVector signerinfo = new ASN1EncodableVector();
            
            // Add the signerInfo version
            //
            signerinfo.add(new DERInteger(signerversion));
            
            v = new ASN1EncodableVector();
            v.add(getIssuer(signCert.getTBSCertificate()));
            v.add(new DERInteger(signCert.getSerialNumber()));
            signerinfo.add(new DERSequence(v));
            
            // Add the digestAlgorithm
            v = new ASN1EncodableVector();
            v.add(new DERObjectIdentifier(digestAlgorithm));
            v.add(new DERNull());
            signerinfo.add(new DERSequence(v));
            
            // Add the digestEncryptionAlgorithm
            v = new ASN1EncodableVector();
            v.add(new DERObjectIdentifier(digestEncryptionAlgorithm));
            v.add(new DERNull());
            signerinfo.add(new DERSequence(v));
            
            // Add the digest
            signerinfo.add(new DEROctetString(digest));
            
            
            // Finally build the body out of all the components above
            ASN1EncodableVector body = new ASN1EncodableVector();
            body.add(new DERInteger(version));
            body.add(digestAlgorithms);
            body.add(contentinfo);
            body.add(new DERTaggedObject(false, 0, dercertificates));
            
            if (crls.size() > 0) {
                v = new ASN1EncodableVector();
                for (Iterator i = crls.iterator();i.hasNext();) {
                    ASN1InputStream t = new ASN1InputStream(new ByteArrayInputStream((((X509CRL)i.next()).getEncoded())));
                    v.add(t.readObject());
                }
                DERSet dercrls = new DERSet(v);
                body.add(new DERTaggedObject(false, 1, dercrls));
            }
            
            // Only allow one signerInfo
            body.add(new DERSet(new DERSequence(signerinfo)));
            
            // Now we have the body, wrap it in it's PKCS7Signed shell
            // and return it
            //
            ASN1EncodableVector whole = new ASN1EncodableVector();
            whole.add(new DERObjectIdentifier(ID_PKCS7_SIGNED_DATA));
            whole.add(new DERTaggedObject(0, new DERSequence(body)));
            
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            
            ASN1OutputStream dout = new ASN1OutputStream(bOut);
            dout.writeObject(new DERSequence(whole));
            dout.close();
            
            return bOut.toByteArray();
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    /**
     * Getter for property reason.
     * @return Value of property reason.
     */
    public String getReason() {
        return this.reason;
    }
    
    /**
     * Setter for property reason.
     * @param reason New value of property reason.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    /**
     * Getter for property location.
     * @return Value of property location.
     */
    public String getLocation() {
        return this.location;
    }
    
    /**
     * Setter for property location.
     * @param location New value of property location.
     */
    public void setLocation(String location) {
        this.location = location;
    }
    
    /**
     * Getter for property signDate.
     * @return Value of property signDate.
     */
    public Calendar getSignDate() {
        return this.signDate;
    }
    
    /**
     * Setter for property signDate.
     * @param signDate New value of property signDate.
     */
    public void setSignDate(Calendar signDate) {
        this.signDate = signDate;
    }
    
    /**
     * Getter for property sigName.
     * @return Value of property sigName.
     */
    public String getSignName() {
        return this.signName;
    }
    
    /**
     * Setter for property sigName.
     * @param sigName New value of property sigName.
     */
    public void setSignName(String signName) {
        this.signName = signName;
    }
    
    public static class X509Name {
        /**
         * country code - StringType(SIZE(2))
         */
        public static final DERObjectIdentifier C = new DERObjectIdentifier("2.5.4.6");

        /**
         * organization - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier O = new DERObjectIdentifier("2.5.4.10");

        /**
         * organizational unit name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier OU = new DERObjectIdentifier("2.5.4.11");

        /**
         * Title
         */
        public static final DERObjectIdentifier T = new DERObjectIdentifier("2.5.4.12");

        /**
         * common name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier CN = new DERObjectIdentifier("2.5.4.3");

        /**
         * device serial number name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier SN = new DERObjectIdentifier("2.5.4.5");

        /**
         * locality name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier L = new DERObjectIdentifier("2.5.4.7");

        /**
         * state, or province name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier ST = new DERObjectIdentifier("2.5.4.8");

        /**
         * Naming attributes of type X520name
         */
        public static final DERObjectIdentifier SURNAME = new DERObjectIdentifier("2.5.4.4");
        public static final DERObjectIdentifier GIVENNAME = new DERObjectIdentifier("2.5.4.42");
        public static final DERObjectIdentifier INITIALS = new DERObjectIdentifier("2.5.4.43");
        public static final DERObjectIdentifier GENERATION = new DERObjectIdentifier("2.5.4.44");
        public static final DERObjectIdentifier UNIQUE_IDENTIFIER = new DERObjectIdentifier("2.5.4.45");

        /**
         * Email address (RSA PKCS#9 extension) - IA5String.
         * <p>Note: if you're trying to be ultra orthodox, don't use this! It shouldn't be in here.
         */
        public static final DERObjectIdentifier EmailAddress = new DERObjectIdentifier("1.2.840.113549.1.9.1");

        /**
         * email address in Verisign certificates
         */
        public static final DERObjectIdentifier E = EmailAddress;

        /*
         * others...
         */
        public static final DERObjectIdentifier DC = new DERObjectIdentifier("0.9.2342.19200300.100.1.25");

        /**
         * LDAP User id.
         */
        public static final DERObjectIdentifier UID = new DERObjectIdentifier("0.9.2342.19200300.100.1.1");

        public static HashMap DefaultSymbols = new HashMap();
        
        static {
            DefaultSymbols.put(C, "C");
            DefaultSymbols.put(O, "O");
            DefaultSymbols.put(T, "T");
            DefaultSymbols.put(OU, "OU");
            DefaultSymbols.put(CN, "CN");
            DefaultSymbols.put(L, "L");
            DefaultSymbols.put(ST, "ST");
            DefaultSymbols.put(SN, "SN");
            DefaultSymbols.put(EmailAddress, "E");
            DefaultSymbols.put(DC, "DC");
            DefaultSymbols.put(UID, "UID");
            DefaultSymbols.put(SURNAME, "SURNAME");
            DefaultSymbols.put(GIVENNAME, "GIVENNAME");
            DefaultSymbols.put(INITIALS, "INITIALS");
            DefaultSymbols.put(GENERATION, "GENERATION");
        }
        
        public HashMap values = new HashMap();

        public X509Name(ASN1Sequence seq) {
            Enumeration e = seq.getObjects();
            
            while (e.hasMoreElements()) {
                ASN1Set set = (ASN1Set)e.nextElement();
                
                for (int i = 0; i < set.size(); i++) {
                    ASN1Sequence s = (ASN1Sequence)set.getObjectAt(i);
                    String id = (String)DefaultSymbols.get(s.getObjectAt(0));
                    if (id == null)
                        continue;
                    ArrayList vs = (ArrayList)values.get(id);
                    if (vs == null) {
                        vs = new ArrayList();
                        values.put(id, vs);
                    }
                    vs.add(((DERString)s.getObjectAt(1)).getString());
                }
            }
        }
        
        public String getField(String name) {
            ArrayList vs = (ArrayList)values.get(name);
            return vs == null ? null : (String)vs.get(0);
        }

        public ArrayList getFieldArray(String name) {
            ArrayList vs = (ArrayList)values.get(name);
            return vs == null ? null : vs;
        }
        
        public HashMap getFields() {
            return values;
        }
        
        public String toString() {
            return values.toString();
        }
    }
}