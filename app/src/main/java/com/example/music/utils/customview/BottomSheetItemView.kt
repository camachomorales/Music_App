package com.example.music.utils.customview

import android.content.Context
import android.util.AttributeSet
import android.net.Uri
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.music.R
import com.squareup.picasso.Picasso

class BottomSheetItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    var ID: String = ""
    var NAME: String = ""
    var IMAGE_URL: String = ""

    private val textView: TextView
    private val iconImageView: ImageView

    init {
        inflate(context, R.layout.bottom_sheet_items_custom_view, this)
        isFocusable = true
        isClickable = true

        textView = findViewById(R.id.text)
        iconImageView = findViewById(R.id.icon)

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.BottomSheetItemView, defStyle, 0)
            val title = a.getString(R.styleable.BottomSheetItemView_title)
            val drawable = a.getDrawable(R.styleable.BottomSheetItemView_android_src)
            val padding = a.getDimensionPixelSize(R.styleable.BottomSheetItemView_srcPadding, 4)
            a.recycle()

            textView.text = title
            iconImageView.setImageDrawable(drawable)
            iconImageView.setPadding(padding, padding, padding, padding)
        }
    }

    constructor(context: Context, string: String, imageUrl: String, id: String) : this(context) {
        textView.text = string
        if (imageUrl.isNotBlank()) Picasso.get().load(Uri.parse(imageUrl)).into(iconImageView)
        ID = id
        NAME = string
        IMAGE_URL = imageUrl
    }

    // Método público para exponer el TextView y poder modificar texto desde actividad
    fun getTitleTextView(): TextView {
        return textView
    }
}
