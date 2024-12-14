package com.lcaohoanq.nocket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProviderName {

    GOOGLE("GOOGLE"),
    FACEBOOK("FACEBOOK"),;

    private final String providerName;

}
