import java.awt.Color
import java.awt.image.BufferedImage
import java.io.Serializable
import javax.imageio.ImageIO
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.androidBuiltInKotlin)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.screenshot)
    alias(libs.plugins.plusKtfmt)
}

android {
    namespace = "com.plusmobileapps.chefmate.ui.storescreenshots"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures { compose = true }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

dependencies {
    implementation(project(":client:ui:public"))
    implementation(project(":client:text:public"))
    implementation(project(":client:aichat:public"))
    implementation(project(":client:aichat:impl"))
    implementation(project(":client:cook:public"))
    implementation(project(":client:cook:impl"))
    implementation(project(":client:grocery:core:public"))
    implementation(project(":client:grocery:core:impl"))
    implementation(project(":client:grocery:autocomplete:public"))
    implementation(project(":client:meal:core:public"))
    implementation(project(":client:meal:core:impl"))
    implementation(project(":client:meal:data:public"))
    implementation(project(":client:recipe:core:public"))
    implementation(project(":client:recipe:core:impl"))
    implementation(project(":client:recipe:data:public"))
    implementation(project(":client:recipe:list:public"))
    implementation(project(":client:recipe:list:impl"))
    implementation(project(":client:toast:public"))
    implementation(project(":client:toast:testing"))
    implementation(libs.arkivanov.decompose.core)

    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
}

// ── Store asset collection ─────────────────────────────────────────────────
//
// `updateDebugScreenshotTest` renders every @PreviewTest in this module into
// src/screenshotTestDebug/reference/. Those PNGs are intermediates (gitignored) — this task turns
// them into the two asset trees fastlane uploads from, and is the single place that enforces the
// stores' hard requirements so a bad asset fails the build instead of a release.

/** A store screenshot slot: which previews feed it, how big they must be, and where they land. */
data class StoreSlot(
    /** Function-name prefix in StoreScreenshots.kt. */
    val prefix: String,
    val expectedWidth: Int,
    val expectedHeight: Int,
    /** Prepended to the output file name; keeps the two Apple slots apart in one flat folder. */
    val fileNamePrefix: String,
    val minCount: Int,
    val maxCount: Int,
) : Serializable // Gradle fingerprints @Input values, which requires Java serialization.

abstract class CollectStoreScreenshotsTask : DefaultTask() {

    @get:InputDirectory abstract val renderedDir: DirectoryProperty

    @get:OutputDirectory abstract val playPhoneDir: DirectoryProperty

    @get:OutputDirectory abstract val playTabletDir: DirectoryProperty

    @get:OutputDirectory abstract val appStoreDir: DirectoryProperty

    @get:Input abstract val slots: MapProperty<String, StoreSlot>

    @TaskAction
    fun collect() {
        val outputs =
            mapOf(
                "PlayPhone" to playPhoneDir.get().asFile,
                "PlayTablet" to playTabletDir.get().asFile,
                "IosPhone" to appStoreDir.get().asFile,
                "IosTablet" to appStoreDir.get().asFile,
            )
        // Wipe first so a scene that was deleted from StoreScreenshots.kt also disappears from the
        // store listing on the next upload, instead of lingering as an orphan file.
        outputs.values.distinct().forEach { dir ->
            dir.listFiles()?.filter { it.extension == "png" }?.forEach { it.delete() }
            dir.mkdirs()
        }

        val namePattern = Regex("""^([A-Za-z]+?)(\d{2})([A-Za-z0-9]*)_[0-9a-f]+_\d+\.png$""")
        val collected = mutableMapOf<String, MutableList<Int>>()
        val rendered =
            renderedDir.get().asFile.walkTopDown().filter { it.isFile && it.extension == "png" }

        rendered.forEach { source ->
            val match =
                namePattern.matchEntire(source.name)
                    ?: throw GradleException(
                        "Unrecognised store screenshot '${source.name}'. Preview functions in " +
                            "StoreScreenshots.kt must be named <Target><NN><Scene>, e.g. " +
                            "PlayPhone01Recipes."
                    )
            val (prefix, index, scene) = match.destructured
            val slot =
                slots.get()[prefix]
                    ?: throw GradleException(
                        "'${source.name}' targets unknown slot '$prefix'. Known slots: " +
                            slots.get().keys.sorted().joinToString()
                    )

            val image = ImageIO.read(source)
            if (image.width != slot.expectedWidth || image.height != slot.expectedHeight) {
                throw GradleException(
                    "${source.name} rendered ${image.width}×${image.height}, but the $prefix slot " +
                        "requires exactly ${slot.expectedWidth}×${slot.expectedHeight}. Check the " +
                        "device spec in StoreDevices.kt — the renderer snaps `dpi` to the nearest " +
                        "standard density bucket, so an off-bucket value is silently ignored."
                )
            }

            val ordinals = collected.getOrPut(prefix) { mutableListOf() }
            val ordinal = index.toInt()
            if (ordinal in ordinals) {
                throw GradleException(
                    "Two $prefix previews both claim position $index. Each scene needs a unique " +
                        "two-digit index within a slot."
                )
            }
            ordinals += ordinal

            // Play requires "24-bit PNG (no alpha)" and the renderer emits 32-bit RGBA, so flatten
            // onto white. Apple accepts either, and uploading one format to both keeps the two
            // trees byte-identical for a given scene.
            val flattened = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
            flattened.createGraphics().apply {
                color = Color.WHITE
                fillRect(0, 0, image.width, image.height)
                drawImage(image, 0, 0, null)
                dispose()
            }

            val snakeScene =
                scene.replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), "_").lowercase().ifEmpty { "scene" }
            val target =
                File(outputs.getValue(prefix), "${slot.fileNamePrefix}${index}_$snakeScene.png")
            ImageIO.write(flattened, "png", target)
            logger.lifecycle("store screenshot: ${target.name} (${image.width}×${image.height})")
        }

        slots.get().forEach { (prefix, slot) ->
            val count = collected[prefix]?.size ?: 0
            if (count < slot.minCount || count > slot.maxCount) {
                throw GradleException(
                    "The $prefix slot has $count screenshot(s); the store requires between " +
                        "${slot.minCount} and ${slot.maxCount}. Add or remove $prefix* previews " +
                        "in StoreScreenshots.kt."
                )
            }
        }
    }
}

val storeSlots =
    mapOf(
        // Play phone: no aspect-ratio rule, 320–3840px per side, longer side ≤ 2× shorter.
        "PlayPhone" to StoreSlot("PlayPhone", 1080, 2100, "", minCount = 2, maxCount = 8),
        // Play 10" tablet: must be exactly 9:16 portrait with the short side ≥ 1080px, and Google
        // asks for at least 4 to demonstrate the large-screen experience.
        "PlayTablet" to StoreSlot("PlayTablet", 1440, 2560, "", minCount = 4, maxCount = 8),
        // App Store Connect resolves the device slot from the pixel size alone.
        "IosPhone" to StoreSlot("IosPhone", 1320, 2868, "iphone_6_9_", minCount = 1, maxCount = 10),
        "IosTablet" to StoreSlot("IosTablet", 2064, 2752, "ipad_13_", minCount = 1, maxCount = 10),
    )

// The only locale with a translated strings.xml. Add a locale here (and a matching render pass)
// when the app actually ships one — uploading en-US art under another locale is worse than
// leaving that locale to inherit the default listing.
val storeLocale = "en-US"

val fastlaneDir = rootProject.layout.projectDirectory.dir("fastlane")

tasks.register<CollectStoreScreenshotsTask>("collectStoreScreenshots") {
    group = "publishing"
    description = "Render the store listing screenshots and lay them out for supply + deliver."

    dependsOn("updateDebugScreenshotTest")

    renderedDir.set(layout.projectDirectory.dir("src/screenshotTestDebug/reference"))
    playPhoneDir.set(fastlaneDir.dir("metadata/android/$storeLocale/images/phoneScreenshots"))
    playTabletDir.set(fastlaneDir.dir("metadata/android/$storeLocale/images/tenInchScreenshots"))
    appStoreDir.set(fastlaneDir.dir("screenshots/$storeLocale"))
    slots.set(storeSlots)
}
