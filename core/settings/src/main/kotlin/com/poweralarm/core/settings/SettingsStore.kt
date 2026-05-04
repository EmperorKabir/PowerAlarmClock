package com.poweralarm.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Reads / writes setting values backed by [DataStore]. Per-alarm overrides live in
 * `AlarmEntity.metadataJson` keyed by the same setting id; consumers should consult both
 * via [observeEffective] (override > global > default).
 */
class SettingsStore(
    private val dataStore: DataStore<Preferences>,
    private val registry: SettingsRegistry,
) {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> observe(descriptorId: String): Flow<T> {
        val descriptor = requireNotNull(registry.byId(descriptorId)) { "Unknown setting '$descriptorId'" }
        return dataStore.data.map { prefs -> readValue(descriptor, prefs) as T }.distinctUntilChanged()
    }

    fun observeEffective(
        descriptorId: String,
        perAlarmOverrides: Map<String, Any>,
    ): Flow<Any> {
        val descriptor = requireNotNull(registry.byId(descriptorId)) { "Unknown setting '$descriptorId'" }
        val override = perAlarmOverrides[descriptorId]
        return if (override != null) {
            kotlinx.coroutines.flow.flowOf(override)
        } else {
            dataStore.data.map { prefs -> readValue(descriptor, prefs) }.distinctUntilChanged()
        }
    }

    suspend fun <T : Any> set(descriptorId: String, value: T) {
        val descriptor = requireNotNull(registry.byId(descriptorId)) { "Unknown setting '$descriptorId'" }
        when (val v = Validators.validate(descriptor, value)) {
            is ValidationResult.Invalid -> throw IllegalArgumentException(v.reason)
            ValidationResult.Ok -> Unit
        }
        dataStore.edit { prefs -> writeValue(descriptor, value, prefs) }
    }

    suspend fun reset(descriptorId: String) {
        val descriptor = requireNotNull(registry.byId(descriptorId)) { "Unknown setting '$descriptorId'" }
        dataStore.edit { prefs -> prefs.remove(keyOf(descriptor)) }
    }

    private fun readValue(descriptor: SettingDescriptor<*>, prefs: Preferences): Any = when (descriptor) {
        is SettingDescriptor.BoolSetting -> prefs[booleanPreferencesKey(descriptor.id)] ?: descriptor.default
        is SettingDescriptor.IntSetting -> prefs[intPreferencesKey(descriptor.id)] ?: descriptor.default
        is SettingDescriptor.FloatSetting -> prefs[floatPreferencesKey(descriptor.id)] ?: descriptor.default
        is SettingDescriptor.StringSetting -> prefs[stringPreferencesKey(descriptor.id)] ?: descriptor.default
        is SettingDescriptor.ColorSetting -> prefs[stringPreferencesKey(descriptor.id)] ?: descriptor.default
        is SettingDescriptor.EnumSetting -> prefs[stringPreferencesKey(descriptor.id)] ?: descriptor.default
        is SettingDescriptor.JsonSetting -> prefs[stringPreferencesKey(descriptor.id)] ?: descriptor.default
    }

    private fun writeValue(
        descriptor: SettingDescriptor<*>,
        value: Any,
        prefs: androidx.datastore.preferences.core.MutablePreferences,
    ) {
        when (descriptor) {
            is SettingDescriptor.BoolSetting -> prefs[booleanPreferencesKey(descriptor.id)] = value as Boolean
            is SettingDescriptor.IntSetting -> prefs[intPreferencesKey(descriptor.id)] = value as Int
            is SettingDescriptor.FloatSetting -> prefs[floatPreferencesKey(descriptor.id)] = value as Float
            is SettingDescriptor.StringSetting,
            is SettingDescriptor.ColorSetting,
            is SettingDescriptor.EnumSetting,
            is SettingDescriptor.JsonSetting,
            -> prefs[stringPreferencesKey(descriptor.id)] = value as String
        }
    }

    private fun keyOf(descriptor: SettingDescriptor<*>): Preferences.Key<*> = when (descriptor) {
        is SettingDescriptor.BoolSetting -> booleanPreferencesKey(descriptor.id)
        is SettingDescriptor.IntSetting -> intPreferencesKey(descriptor.id)
        is SettingDescriptor.FloatSetting -> floatPreferencesKey(descriptor.id)
        is SettingDescriptor.StringSetting,
        is SettingDescriptor.ColorSetting,
        is SettingDescriptor.EnumSetting,
        is SettingDescriptor.JsonSetting,
        -> stringPreferencesKey(descriptor.id)
    }
}
