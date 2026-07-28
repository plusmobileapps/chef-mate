package com.plusmobileapps.chefmate.ui.storescreenshots

/**
 * Preview device specs whose rendered pixel size is exactly what each store slot requires.
 *
 * The preview renderer rasterises at the device's full density, so the output pixel size is `dp *
 * (dpi / 160)`. Every spec below is chosen so that product is an exact integer — if you change one,
 * keep it exact, because `collectStoreScreenshots` fails the build on any image whose dimensions
 * don't match its slot to the pixel.
 *
 * **Only standard Android density buckets survive.** The renderer snaps `dpi` to the nearest
 * bucket, so an off-bucket value is silently ignored and the image comes out the wrong size:
 * `dpi=400` rendered at 420 (density 2.625) and produced 1134×2016 instead of 1080×1920. Stick to
 * 160 / 240 / 320 / 480 / 640.
 *
 * Store constraints these satisfy:
 * * **Play phone** has no aspect-ratio rule — only 320–3840px per side, with the longer side no
 *   more than twice the shorter. **Play 10" tablet** does require exactly 9:16 portrait, with the
 *   shortest side ≥ 1080px.
 * * **App Store Connect** matches a screenshot to a device slot by its pixel size alone. 2064×2752
 *   is used for the iPad rather than 2048×2732 because fastlane lists the latter as ambiguous
 *   between "iPad Pro 12.9"" and "iPad 13"" and refuses to guess.
 */
internal object StoreDevices {

    /** Play "Phone" slot — 1080 × 2100 (1.94:1, safely inside Play's 2× cap). */
    const val PLAY_PHONE = "spec:width=360dp,height=700dp,dpi=480"

    /** Play "10-inch tablet" slot — 1440 × 2560 (9:16, shortest side ≥ 1080). */
    const val PLAY_TABLET = "spec:width=720dp,height=1280dp,dpi=320"

    /** App Store 6.9" iPhone slot — 1320 × 2868 (iPhone 16 Pro Max, 440 × 956 pt @3x). */
    const val IOS_PHONE = "spec:width=440dp,height=956dp,dpi=480"

    /** App Store 13" iPad slot — 2064 × 2752 (iPad Pro 13", 1032 × 1376 pt @2x). */
    const val IOS_TABLET = "spec:width=1032dp,height=1376dp,dpi=320"
}
