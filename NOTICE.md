# NOTICE

Split from `apps/panvk-test` of https://github.com/zenithblue-oss/panvk-kbase-android
(see that repository's `NOTICE.md` for the full driver-side notice).

## 1. MIT: PanProbe's own code

App, native test programs, scripts and docs authored for this project are MIT-licensed unless a file
header states otherwise. See `LICENSE`.

## 2. Vulkan headers: vendored (Apache-2.0 OR MIT)

`third_party/vulkan-headers/include/` holds Khronos Vulkan headers (`vulkan/`, `vk_video/`), header
version 363, `SPDX-License-Identifier: Apache-2.0 OR MIT`, Copyright The Khronos Group Inc. Unmodified.

## 3. Bundled driver (not vendored in git)

Release APKs bundle the PanVK Kbase driver pinned in `bundled-driver.json`. It is Mesa-derived; source
and notices are in https://github.com/zenithblue-oss/panvk-kbase-android.
