package gr.hua.dit.project.web.rest.model;

import gr.hua.dit.project.web.rest.ClientAuthResource;

/**
 * @see ClientAuthResource
 */
public record ClientTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
