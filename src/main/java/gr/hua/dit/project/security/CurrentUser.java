package gr.hua.dit.project.security;

import gr.hua.dit.project.core.model.PersonType;

public record CurrentUser (Long id, String username, PersonType type) {}
