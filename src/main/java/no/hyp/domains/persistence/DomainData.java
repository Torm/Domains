package no.hyp.domains.persistence;

import javax.annotation.Nullable;

public class DomainData {

    final String domainKey;

    final String defaultRole;

    @Nullable String displayName;

    public DomainData(String domainKey, String defaultRole, @Nullable String displayName) {
        this.domainKey = domainKey;
        this.defaultRole = defaultRole;
        this.displayName = displayName;
    }

}
