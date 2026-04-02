package org.dhis2.usescases.notifications.domain
// EyeSeeTea customization - Notifications system

interface UserRepository {
    fun getCurrentUser(): User
}