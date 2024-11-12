package com.uvers.unisehat.models

import java.util.Date

data class AgendaItem(
    val identifier: String = "",
    val title: String = "",
    val date: Date = Date(),
    val description: String = "",
)