package com.poweralarm.core.settings

/**
 * Authoritative list of every user-configurable variable in the app.
 * Concrete population happens in [RegistrySeed]; this interface keeps consumers decoupled.
 */
interface SettingsRegistry {
    fun all(): List<SettingDescriptor<*>>
    fun byId(id: String): SettingDescriptor<*>?
    fun byGroup(groupPathPrefix: String): List<SettingDescriptor<*>>
}

class InMemorySettingsRegistry(
    descriptors: List<SettingDescriptor<*>>,
) : SettingsRegistry {
    private val byId: Map<String, SettingDescriptor<*>> = descriptors.associateBy { it.id }

    init {
        require(byId.size == descriptors.size) {
            val dups = descriptors.groupBy { it.id }.filter { it.value.size > 1 }.keys
            "Duplicate setting ids: $dups"
        }
        descriptors.forEach { d ->
            d.dependsOn.forEach { dep ->
                require(byId.containsKey(dep)) { "Unknown dependency '$dep' from '${d.id}'" }
            }
        }
    }

    override fun all(): List<SettingDescriptor<*>> = byId.values.toList()
    override fun byId(id: String): SettingDescriptor<*>? = byId[id]
    override fun byGroup(groupPathPrefix: String): List<SettingDescriptor<*>> =
        byId.values.filter { it.groupPath == groupPathPrefix || it.groupPath.startsWith("$groupPathPrefix.") }
}
