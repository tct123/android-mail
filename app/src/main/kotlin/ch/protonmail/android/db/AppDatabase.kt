/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.db

import android.content.Context
import androidx.room.Database
import androidx.room.TypeConverters
import me.proton.core.account.data.db.AccountConverters
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.entity.SessionDetailsEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.contact.data.local.db.ContactConverters
import me.proton.core.contact.data.local.db.ContactDatabase
import me.proton.core.contact.data.local.db.entity.ContactCardEntity
import me.proton.core.contact.data.local.db.entity.ContactEmailEntity
import me.proton.core.contact.data.local.db.entity.ContactEmailLabelEntity
import me.proton.core.contact.data.local.db.entity.ContactEntity
import me.proton.core.crypto.android.keystore.CryptoConverters
import me.proton.core.data.room.db.BaseDatabase
import me.proton.core.data.room.db.CommonConverters
import me.proton.core.eventmanager.data.db.EventManagerConverters
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.eventmanager.data.entity.EventMetadataEntity
import me.proton.core.humanverification.data.db.HumanVerificationConverters
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.humanverification.data.entity.HumanVerificationEntity
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.key.data.entity.KeySaltEntity
import me.proton.core.key.data.entity.PublicAddressEntity
import me.proton.core.key.data.entity.PublicAddressKeyEntity
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.mailsettings.data.entity.MailSettingsEntity
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserConverters
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.AddressKeyEntity
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.data.entity.UserKeyEntity
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsConverters
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import me.proton.core.usersettings.data.entity.OrganizationEntity
import me.proton.core.usersettings.data.entity.OrganizationKeysEntity
import me.proton.core.usersettings.data.entity.UserSettingsEntity

@Database(
    entities = [
        // account-data
        AccountEntity::class,
        AccountMetadataEntity::class,
        SessionEntity::class,
        SessionDetailsEntity::class,
        // user-data
        UserEntity::class,
        UserKeyEntity::class,
        AddressEntity::class,
        AddressKeyEntity::class,
        // key-data
        KeySaltEntity::class,
        PublicAddressEntity::class,
        PublicAddressKeyEntity::class,
        // human-verification
        HumanVerificationEntity::class,
        // mail-settings
        MailSettingsEntity::class,
        // user-settings
        UserSettingsEntity::class,
        // organization
        OrganizationEntity::class,
        OrganizationKeysEntity::class,
        // contact
        ContactEntity::class,
        ContactCardEntity::class,
        ContactEmailEntity::class,
        ContactEmailLabelEntity::class,
        // event-manager
        EventMetadataEntity::class,
    ],
    version = AppDatabase.version,
    exportSchema = true
)
@TypeConverters(
    CommonConverters::class,
    AccountConverters::class,
    UserConverters::class,
    CryptoConverters::class,
    HumanVerificationConverters::class,
    UserSettingsConverters::class,
    ContactConverters::class,
    EventManagerConverters::class,
)
abstract class AppDatabase :
    BaseDatabase(),
    AccountDatabase,
    UserDatabase,
    AddressDatabase,
    KeySaltDatabase,
    HumanVerificationDatabase,
    PublicAddressDatabase,
    MailSettingsDatabase,
    UserSettingsDatabase,
    OrganizationDatabase,
    ContactDatabase,
    EventMetadataDatabase {

    companion object {
        const val name = "db-mail"
        const val version = 2

        private val migrations = listOf(
            AppDatabaseMigrations.MIGRATION_1_2,
        )

        fun buildDatabase(context: Context): AppDatabase =
            databaseBuilder<AppDatabase>(context, name)
                .apply { migrations.forEach { addMigrations(it) } }
                .build()
    }
}
