package org.example.tophits.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.relational.core.mapping.NamingStrategy
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty

@Configuration
class JdbcConfig {

    @Bean
    fun namingStrategy(): NamingStrategy {
        return object : NamingStrategy {
            override fun getTableName(type: Class<*>): String {
                return type.simpleName.lowercase() + "s"
            }

            override fun getColumnName(property: RelationalPersistentProperty): String {
                // Convert camelCase to snake_case
                return property.name.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
            }
        }
    }
}