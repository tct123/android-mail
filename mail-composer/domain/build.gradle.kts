/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") version Versions.Gradle.kotlinGradlePlugin
}

setAsHiltModule()

android {
    namespace = "ch.protonmail.android.mailcomposer.domain"
    compileSdk = Config.compileSdk

    defaultConfig {
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(Proton.Core.user)
    implementation(Proton.Core.mailSettings)
    implementation(Proton.Core.mailSendPreferences)

    implementation(Dependencies.moduleDomainLibs)
    implementation(Proton.Core.user)
    implementation(Proton.Core.label)
    implementation(Jsoup.jsoup)
    implementation(project(":mail-message:domain"))
    implementation(project(":mail-common:domain"))
    implementation(project(":mail-label:domain"))
    implementation(project(":mail-pagination:domain"))
    implementation(project(":mail-settings:domain"))

    testImplementation(Dependencies.testLibs)
    // Used to access sample test data (here instead of test-data as shared with compose previews / android tests)
    testImplementation(project(":mail-common:domain"))
    testImplementation(project(":test:test-data"))
    testImplementation(project(":test:utils"))
}
