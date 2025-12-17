package gr.hua.dit.project.core.security;

import gr.hua.dit.project.core.model.PersonType;

/**
 * @see CurrentUserProvider
 */
public record CurrentUser(long id, String username,String email, PersonType type) {}
