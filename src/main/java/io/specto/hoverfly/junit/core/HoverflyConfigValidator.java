package io.specto.hoverfly.junit.core;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Validate user-input {@link HoverflyConfig} before it is used by {@link Hoverfly}
 */
class HoverflyConfigValidator {

    private static final int DEFAULT_PROXY_PORT = 8500;
    private static final int DEFAULT_ADMIN_PORT = 8888;

    /**
     * Sanity checking hoverfly configs and assign port number if necessary
     */
    HoverflyConfig validate(HoverflyConfig hoverflyConfig) {

        if (hoverflyConfig == null) {
            throw new IllegalArgumentException("HoverflyConfig cannot be null.");
        }

        boolean isKeyBlank = StringUtils.isBlank(hoverflyConfig.getSslKeyPath());
        boolean isCertBlank = StringUtils.isBlank(hoverflyConfig.getSslCertificatePath());
        if (isKeyBlank && !isCertBlank || !isKeyBlank && isCertBlank) {
            throw new IllegalArgumentException("Both SSL key and certificate files are required to override the default Hoverfly SSL.");
        }

        if (hoverflyConfig.isRemoteInstance()) {
            if (!isKeyBlank && !isCertBlank) {
                throw new IllegalArgumentException("Attempt to configure SSL on remote instance is prohibited.");
            }

            if (hoverflyConfig.getHost() != null && hoverflyConfig.getHost().startsWith("http")) {
                try {
                    URI uri = new URI(hoverflyConfig.getHost());
                    hoverflyConfig.useRemoteInstance(uri.getHost());
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Remote hoverfly hostname is not valid: " + hoverflyConfig.getHost());
                }
            }
        }

        if (hoverflyConfig.getProxyPort() == 0) {
            hoverflyConfig.proxyPort(hoverflyConfig.isRemoteInstance() ? DEFAULT_PROXY_PORT : findUnusedPort());
        }

        if (hoverflyConfig.getAdminPort() == 0) {
            hoverflyConfig.adminPort(hoverflyConfig.isRemoteInstance() ? DEFAULT_ADMIN_PORT : findUnusedPort());
        }



        // TODO validate supported SSL file format

        return hoverflyConfig;
    }


    /**
     * Looks for an unused port on the current machine
     */
    private static int findUnusedPort() {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Cannot find available port", e);
        }
    }
}
