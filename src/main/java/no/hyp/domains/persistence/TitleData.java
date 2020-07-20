package no.hyp.domains.persistence;

public class TitleData {

    final String domainKey;

    final String playerUuid;

    final String role;

    final String title;

    public TitleData(String domainKey, String playerUuid, String role, String title) {
        this.domainKey = domainKey;
        this.playerUuid = playerUuid;
        this.role = role;
        this.title = title;
    }

}
