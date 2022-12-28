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

package ch.protonmail.android.testdata.message

import ch.protonmail.android.mailmessage.domain.entity.MessageBody
import ch.protonmail.android.mailmessage.domain.entity.MessageId
import ch.protonmail.android.mailmessage.domain.entity.UnsubscribeMethod
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample
import ch.protonmail.android.testdata.message.MessageTestData.RAW_MESSAGE_ID
import ch.protonmail.android.testdata.user.UserIdTestData.userId

object MessageBodyTestData {

    const val RAW_MESSAGE_BODY = "This is a raw message body."

    val messageBody = MessageBody(
        userId = userId,
        messageId = MessageId(RAW_MESSAGE_ID),
        body = RAW_MESSAGE_BODY,
        header = "",
        parsedHeaders = emptyMap(),
        attachments = emptyList(),
        mimeType = "",
        spamScore = "",
        replyTo = RecipientSample.John,
        replyTos = emptyList(),
        unsubscribeMethods = UnsubscribeMethod(null, null, null)
    )
}
