package com.poweralarm.core.domain.model

import kotlinx.serialization.json.Json

val DomainJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    classDiscriminator = "kind"
    prettyPrint = false
}
