import java.net.URI
import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Bundled driver: pinned released binary (../bundled-driver.json), downloaded at build time and
// sha256-checked; never committed. Override with -PpanvkSo=<path> (skips the pin check).
val bundledDriverJsonFile = File(rootDir, "bundled-driver.json")
@Suppress("UNCHECKED_CAST")
val bundledDriver = groovy.json.JsonSlurper().parse(bundledDriverJsonFile) as Map<String, Any?>
@Suppress("UNCHECKED_CAST")
val bundledRelease = bundledDriver["release"] as Map<String, Any?>
val pinnedSha = bundledRelease["sha256"] as String
val pinnedUrl = "https://github.com/${bundledRelease["repo"]}/releases/download/${bundledRelease["tag"]}/${bundledRelease["asset"]}"
val pinnedCache = File(System.getProperty("user.home"), ".cache/panprobe/${pinnedSha}.so")

fun sha256Of(f: File): String {
    val md = MessageDigest.getInstance("SHA-256")
    f.inputStream().use { ins ->
        val buf = ByteArray(1 shl 16)
        while (true) { val n = ins.read(buf); if (n < 0) break; md.update(buf, 0, n) }
    }
    return md.digest().joinToString("") { "%02x".format(it) }
}

val panvkSoProp = providers.gradleProperty("panvkSo").orNull
val panvkSoFile = if (panvkSoProp != null) file(panvkSoProp) else pinnedCache

val checkPanvkSo = tasks.register("checkPanvkSo") {
    inputs.property("panvkSoPath", panvkSoFile.absolutePath)
    inputs.property("pinnedSha", pinnedSha)
    outputs.upToDateWhen { panvkSoFile.exists() && (panvkSoProp != null || sha256Of(panvkSoFile) == pinnedSha) }
    doLast {
        if (panvkSoProp != null) {
            if (!panvkSoFile.exists()) throw GradleException("panvkSo not found: ${panvkSoFile.absolutePath}")
            if (sha256Of(panvkSoFile) != pinnedSha) logger.warn("WARNING: -PpanvkSo sha256 != bundled-driver.json pin ($pinnedSha): APK label will not match the bundled .so")
            return@doLast
        }
        if (!(pinnedCache.exists() && sha256Of(pinnedCache) == pinnedSha)) {
            pinnedCache.parentFile.mkdirs()
            val tmp = File(pinnedCache.parentFile, "${pinnedSha}.part")
            logger.lifecycle("Downloading bundled driver $pinnedUrl")
            URI(pinnedUrl).toURL().openStream().use { ins -> tmp.outputStream().use { ins.copyTo(it) } }
            val got = sha256Of(tmp)
            if (got != pinnedSha) {
                tmp.delete()
                throw GradleException("Bundled driver sha256 mismatch: expected $pinnedSha, got $got")
            }
            tmp.renameTo(pinnedCache)
        }
    }
}

val copyPanvkSo = tasks.register<Copy>("copyPanvkSo") {
    dependsOn(checkPanvkSo)
    from(panvkSoFile)
    into(file("build/generated/panvkJni/arm64-v8a"))
    rename { "libvulkan_panfrost.so" }
}

val checkBundledDriverJson = tasks.register("checkBundledDriverJson") {
    inputs.property("bundledDriverJsonPath", bundledDriverJsonFile.absolutePath)
    outputs.upToDateWhen { bundledDriverJsonFile.exists() }
    doLast {
        if (!bundledDriverJsonFile.exists()) {
            throw GradleException("Bundled driver JSON not found at: ${bundledDriverJsonFile.absolutePath}")
        }
    }
}

val copyBundledDriverJson = tasks.register<Copy>("copyBundledDriverJson") {
    dependsOn(checkBundledDriverJson)
    from(bundledDriverJsonFile)
    into(file("build/generated/panvkAssets"))
}

tasks.named("preBuild") {
    dependsOn(copyPanvkSo)
    dependsOn(copyBundledDriverJson)
}

android {
    namespace = "dev.zenithblue.panvktest"
    compileSdk = 36
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "dev.zenithblue.panvktest"
        minSdk = 29
        targetSdk = 36
        versionCode = 7
        versionName = "1.2.3"

        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    sourceSets.getByName("main") {
        jniLibs.directories.add("build/generated/panvkJni")
        assets.directories.add("build/generated/panvkAssets")
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.02.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
}
