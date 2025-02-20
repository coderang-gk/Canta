package org.samo_lego.canta.ui.dialog

import android.content.Context
import org.samo_lego.canta.R
import org.samo_lego.canta.util.BloatData
import org.samo_lego.canta.util.RemovalRecommendation


fun cantaBloatData(context: Context): BloatData {
    return BloatData(
        installData = null,
        description = context.getString(R.string.canta_description),
        removal = RemovalRecommendation.RECOMMENDED,
    )
}