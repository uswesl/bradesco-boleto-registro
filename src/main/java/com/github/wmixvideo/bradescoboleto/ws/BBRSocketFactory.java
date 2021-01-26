package com.github.wmixvideo.bradescoboleto.ws;

import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.apache.commons.net.util.TrustManagerUtils;

import com.github.wmixvideo.bradescoboleto.BBRConfig;

class BBRSocketFactory {

    private final SSLContext context;

    BBRSocketFactory(final BBRConfig config) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        this.context = BBRSocketFactory.createSSLContext(config);
    }

    public SSLContext getContext() {
        return this.context;
    }

    private static SSLContext createSSLContext(final BBRConfig config) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        final KeyManager[] keyManagers = BBRSocketFactory.createKeyManagers(config);
        TrustManager[] trustManagers = null;
        
        try {
        	trustManagers = new TrustManager[] { TrustManagerUtils.getDefaultTrustManager(config.getCadeiaCertificadosKeyStore())};
        } catch (GeneralSecurityException e) {
        	e.printStackTrace();
        }

        final SSLContext sslContext = SSLContext.getInstance(config.getSSLProtocolo());
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext;
    }

    private static KeyManager[] createKeyManagers(final BBRConfig config) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        final String alias = config.getCertificadoKeyStore().aliases().nextElement();
        final X509Certificate certificate = (X509Certificate) config.getCertificadoKeyStore().getCertificate(alias);
        final PrivateKey privateKey = (PrivateKey) config.getCertificadoKeyStore().getKey(alias, config.getCertificadoSenha().toCharArray());
        return new KeyManager[] { new BBRKeyManager(certificate, privateKey) };
    }

    private static class BBRKeyManager implements X509KeyManager {
        private final X509Certificate certificate;
        private final PrivateKey privateKey;

        BBRKeyManager(final X509Certificate certificate, final PrivateKey privateKey) {
            this.certificate = certificate;
            this.privateKey = privateKey;
        }

        @Override
        public String chooseClientAlias(final String[] arg0, final Principal[] arg1, final Socket arg2) {
            return this.certificate.getIssuerDN().getName();
        }

        @Override
        public String chooseServerAlias(final String arg0, final Principal[] arg1, final Socket arg2) {
            return null;
        }

        @Override
        public X509Certificate[] getCertificateChain(final String arg0) {
            return new X509Certificate[] { this.certificate };
        }

        @Override
        public String[] getClientAliases(final String arg0, final Principal[] arg1) {
            return new String[] { this.certificate.getIssuerDN().getName() };
        }

        @Override
        public PrivateKey getPrivateKey(final String arg0) {
            return this.privateKey;
        }

        @Override
        public String[] getServerAliases(final String arg0, final Principal[] arg1) {
            return null;
        }
    }
}
