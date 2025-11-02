package com.example.music.utils.customview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch
import com.example.music.R

class MaterialCustomSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var textHead: String? = null
    private var textOn: String? = null
    private var textOff: String? = null
    private var checked: Boolean = false

    private val textHeadView: TextView
    private val textDescView: TextView
    private val materialSwitch: MaterialSwitch

    private var onCheckChangedListener: OnCheckChangeListener? = null

    init {
        inflate(context, R.layout.material_custom_switch, this)

        textHeadView = findViewById(R.id.text_head)
        textDescView = findViewById(R.id.text_desc)
        materialSwitch = findViewById(R.id.materialSwitch)
        findViewById<LinearLayout>(R.id.root).setOnClickListener { materialSwitch.toggle() }

        materialSwitch.setOnCheckedChangeListener { _, isChecked ->
            textDescView.text = if (isChecked) textOn else textOff
            onCheckChangedListener?.onCheckChanged(isChecked)
        }

        attrs?.let {
            val a: TypedArray = context.obtainStyledAttributes(it, R.styleable.MaterialCustomSwitch, defStyle, 0)

            textHead = a.getString(R.styleable.MaterialCustomSwitch_textHead)
            textOn = a.getString(R.styleable.MaterialCustomSwitch_textOn)
            textOff = a.getString(R.styleable.MaterialCustomSwitch_textOff)
            checked = a.getBoolean(R.styleable.MaterialCustomSwitch_checked, false)

            a.recycle()
        }

        textHeadView.text = textHead
        textDescView.text = if (checked) textOn else textOff
        materialSwitch.isChecked = checked
    }

    fun setOnCheckChangeListener(listener: OnCheckChangeListener) {
        onCheckChangedListener = listener
    }

    fun setChecked(checked: Boolean) {
        materialSwitch.isChecked = checked
    }

    interface OnCheckChangeListener {
        fun onCheckChanged(isChecked: Boolean)
    }
}
