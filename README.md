# Virio

Android TV and mobile baseline adapted from VUEO-NEXT. This baseline retains the VUEO UI and runtime. The Netflix-style UI and built-in provider configuration are subsequent work.

| Module | Application ID |
| --- | --- |
| TV | `com.virio.tv` |
| MOB | `com.virio.mob` |

Place both supplied YAML files in `.github/workflows/`. Add repository secrets `VIRIO_KEYSTORE_BASE64` and `VIRIO_KEYSTORE_PASSWORD`. The signing keystore must use alias `virio` and the same password for the key and keystore.

Upload a ZIP patch to `main` to start Apply ZIP Patch. After it commits the patch, Virio Smart Build and Publish builds the affected app and updates the `virio-dev` release. Shared changes build both apps. A manual Smart Build and Publish run and the first release build both APKs.

For local debug builds with Android SDK 36 and JDK 17, use `bash ./gradlew :tv:assembleDebug` or `bash ./gradlew :mob:assembleDebug`.

Supabase sync, Trakt authentication, and the update feed require Virio-specific configuration. Keep signing credentials in GitHub Actions Secrets. Credentials that must remain private belong on a backend, since values embedded in an APK can be extracted.

Existing third-party notices are included under `LICENSES/` and in `THIRD_PARTY_COLLECTIONS.md`.
