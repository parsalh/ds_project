package gr.hua.dit.project.core.security;

import gr.hua.dit.project.core.model.PersonType;

public record CurrentUser(long id, String username, PersonType type) {}
