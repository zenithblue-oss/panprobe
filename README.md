<p align="center"><img src="docs/panprobe-logo-512.png" width="128" alt="PanProbe logo"></p>

# PanProbe

Vulkan feature/extension info and on-device driver tests for the
[PanVK Kbase driver](https://github.com/zenithblue-oss/panvk-kbase-android) on Mali CSF GPUs
(tested: Mali-G615). Runs the native test programs against the bundled driver, shows the results,
and can upload a log zip for bug reports.

Package `dev.zenithblue.panvktest`, minSdk 29. Sibling app: [PanPlay](https://github.com/zenithblue-oss/panplay).

## Install

Download the APK from [Releases](https://github.com/zenithblue-oss/panprobe/releases).

## Build

```sh
export ANDROID_HOME=/path/to/android-sdk     # NDK 29.0.14206865, CMake 3.22.1
./gradlew :app:assembleDebug
```

- The driver `.so` is not committed. `bundled-driver.json` pins a driver release in the driver repo;
  the build downloads it, verifies sha256, and caches it in `~/.cache/panprobe/`.
  Use `-PpanvkSo=/path/to/libvulkan_panfrost.so` to bundle your own build.
- Native test programs live in `tests/dxvk/vulkan/` and `device/`; shared Vulkan headers are vendored in
  `third_party/vulkan-headers/` (Khronos, Apache-2.0 OR MIT).
- `tools/gen_vkinfo.py --registry vk.xml --header vulkan_core.h` regenerates
  `app/src/main/cpp/vkinfo_gen.h` (needs a Mesa checkout's `vk.xml` and `vulkan_core.h`).

## History

Split from `apps/panvk-test` of
[zenithblue-oss/panvk-kbase-android](https://github.com/zenithblue-oss/panvk-kbase-android) with full
history (releases up to 1.2.2). Old release links in that repo point here.

## License

MIT (`LICENSE`); vendored Vulkan headers keep their Khronos license. See `NOTICE.md`.
