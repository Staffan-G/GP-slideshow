package com.staffang.gpslideshow.picker

data class PickerMediaItem(
    val id: String,
    val isVideo: Boolean,
    val baseUrl: String,
    val mimeType: String
)

data class PickerSession(
    val id: String,
    val pickerUri: String,
    val pollIntervalMs: Long
)
